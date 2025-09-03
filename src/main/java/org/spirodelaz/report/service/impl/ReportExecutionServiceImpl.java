package org.spirodelaz.report.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.spirodelaz.report.dto.ExecuteReportRequest;
import org.spirodelaz.report.dto.ExecuteReportResponse;
import org.spirodelaz.report.entity.QueryDefinitionEntity;
import org.spirodelaz.report.service.QueryDefinitionService;
import org.spirodelaz.report.service.QueryResultDataService;
import org.spirodelaz.report.service.ReportExecutionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ReportExecutionServiceImpl implements ReportExecutionService {

    private final JdbcTemplate jdbcTemplate;
    private final QueryDefinitionService queryDefinitionService;
    private final QueryResultDataService queryResultDataService;
    private final ObjectMapper objectMapper;

    public ReportExecutionServiceImpl(JdbcTemplate jdbcTemplate,
                                      QueryDefinitionService queryDefinitionService,
                                      QueryResultDataService queryResultDataService,
                                      ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryDefinitionService = queryDefinitionService;
        this.queryResultDataService = queryResultDataService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ExecuteReportResponse execute(ExecuteReportRequest request) {
        String sql = request.getSqlText();
        String chartTypeCode = request.getChartTypeCode();

        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("sqlText is required.");
        }
        if (chartTypeCode == null || chartTypeCode.isBlank()) {
            throw new IllegalArgumentException("chartTypeCode is required.");
        }

        log.info("Executing report SQL: {}", sql);

        // 1. 执行 SQL
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        // 2. 构造返回体
        ExecuteReportResponse response = buildResponse(rows);

        // 3. 保存查询定义和结果
        try {
            String resultJson = objectMapper.writeValueAsString(response);

            QueryDefinitionEntity queryDef = queryDefinitionService.findBySqlText(sql)
                    .orElseGet(() -> {
                        QueryDefinitionEntity newQueryDef = new QueryDefinitionEntity();
                        newQueryDef.setSqlText(sql);
                        newQueryDef.setCreatedAt(LocalDateTime.now());
                        newQueryDef.setUpdatedAt(LocalDateTime.now());
                        return queryDefinitionService.save(newQueryDef);
                    });

            queryResultDataService.saveOrUpdateResult(queryDef.getId(), resultJson);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert result to JSON for saving.", e);
        }

        return response;
    }

    /**
     * 将查询结果转换为 columns + values 格式
     */
    private ExecuteReportResponse buildResponse(List<Map<String, Object>> rows) {
        ExecuteReportResponse response = new ExecuteReportResponse();

        if (rows == null || rows.isEmpty()) {
            response.setColumns(Collections.emptyList());
            response.setValues(Collections.emptyList());
            return response;
        }

        // 取第一行，确定列名
        Map<String, Object> firstRow = rows.get(0);

        // 构造列元数据
        List<ExecuteReportResponse.ColumnMeta> columns = new ArrayList<>();
        for (String colName : firstRow.keySet()) {
            ExecuteReportResponse.ColumnMeta col = new ExecuteReportResponse.ColumnMeta();
            col.setColumn(colName);
            columns.add(col);
        }
        response.setColumns(columns);

        // 构造值
        List<List<ExecuteReportResponse.CellValue>> values = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            List<ExecuteReportResponse.CellValue> rowValues = new ArrayList<>();
            for (ExecuteReportResponse.ColumnMeta col : columns) {
                Object val = row.get(col.getColumn());
                ExecuteReportResponse.CellValue cell = new ExecuteReportResponse.CellValue();
                cell.setValue(val);
                rowValues.add(cell);
            }
            values.add(rowValues);
        }
        response.setValues(values);

        return response;
    }
}
