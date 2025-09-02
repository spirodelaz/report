package org.spirodelaz.report.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.spirodelaz.report.dto.ExecuteReportRequest;
import org.spirodelaz.report.dto.ExecuteReportResponse;
import org.spirodelaz.report.entity.ChartTypeEntity;
import org.spirodelaz.report.entity.QueryChartMappingEntity;
import org.spirodelaz.report.entity.QueryDefinitionEntity;
import org.spirodelaz.report.service.*;
import org.spirodelaz.report.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportExecutionServiceImpl implements ReportExecutionService {

    private final JdbcTemplate jdbcTemplate;
    private final QueryDefinitionService queryDefinitionService;
    private final ChartTypeService chartTypeService;
    private final QueryChartMappingService queryChartMappingService;
    private final QueryResultDataService queryResultDataService;
    private final ObjectMapper objectMapper;

    public ReportExecutionServiceImpl(JdbcTemplate jdbcTemplate,
                                      QueryDefinitionService queryDefinitionService,
                                      ChartTypeService chartTypeService,
                                      QueryChartMappingService queryChartMappingService,
                                      QueryResultDataService queryResultDataService,
                                      ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryDefinitionService = queryDefinitionService;
        this.chartTypeService = chartTypeService;
        this.queryChartMappingService = queryChartMappingService;
        this.queryResultDataService = queryResultDataService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ExecuteReportResponse execute(ExecuteReportRequest request) {
        String sql = request.getSqlText();
        String chartTypeCode = request.getChartTypeCode();

        // 1. 规范化 SQL 文本
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("sqlText is required.");
        }
        sql = sql.replaceAll("\\s+", " ").trim();

        if (chartTypeCode == null || chartTypeCode.isBlank()) {
            throw new IllegalArgumentException("chartTypeCode is required.");
        }

        // 2. 查找图表类型
        ChartTypeEntity chartType = chartTypeService.findByCode(chartTypeCode)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported chart type: " + chartTypeCode));

        // 3. 执行 SQL 并获取原始结果。如果 SQL 无效，这里会抛出异常
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        // 4. 针对'indicator'图表类型，如果返回多行数据，则重新执行聚合查询
        if ("indicator".equals(chartTypeCode) && rows.size() > 1) {
            Map<String, String> smartMapping = smartMapColumns(rows, chartType);
            String valueColumn = smartMapping.get("value");
            if (valueColumn != null) {
                String newSql = "SELECT SUM(\"" + valueColumn + "\") AS value FROM (" + sql + ") AS a";
                rows = jdbcTemplate.queryForList(newSql);
            }
        }

        // 5. 再次查找或保存查询定义 (执行成功后再进行持久化操作)
        String finalSql = sql;
        QueryDefinitionEntity queryDef = queryDefinitionService.findBySqlText(sql)
                .orElseGet(() -> {
                    QueryDefinitionEntity newQueryDef = new QueryDefinitionEntity();
                    newQueryDef.setSqlText(finalSql);
                    newQueryDef.setCreatedAt(LocalDateTime.now());
                    newQueryDef.setUpdatedAt(LocalDateTime.now());
                    return queryDefinitionService.save(newQueryDef);
                });

        // 6. 确定列映射
        Map<String, String> mapping = new HashMap<>();

        Optional<QueryChartMappingEntity> mappingEntityOpt =
                queryChartMappingService.findByQueryIdAndChartTypeCode(queryDef.getId(), chartType.getTypeCode());
        if (mappingEntityOpt.isPresent()) {
            String json = mappingEntityOpt.get().getColumnMappings();
            mapping = JsonUtils.fromJson(json, new TypeReference<Map<String, String>>() {});
        }

        if (mapping == null || mapping.isEmpty()) {
            mapping = smartMapColumns(rows, chartType);
        }

        if (mapping.isEmpty()) {
            mapping = JsonUtils.fromJson(chartType.getDefaultMappingTemplate(), new TypeReference<Map<String, String>>() {});
            if (mapping == null) mapping = new HashMap<>();
        }

        // 7. 保存查询结果到数据库
        try {
            String resultJson = objectMapper.writeValueAsString(rows);
            queryResultDataService.saveOrUpdateResult(queryDef.getId(), resultJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert result to JSON for saving.", e);
        }

        // 8. 格式化响应
        ExecuteReportResponse resp = new ExecuteReportResponse();
        resp.setChartType(chartType.getTypeCode());
        resp.setRawResult(rows);
        resp.setUsedMapping(mapping);

        switch (chartType.getTypeCode()) {
            case "indicator":
                String valueField = mapping.getOrDefault("value", "value");
                Object indicatorValueObj = null;
                if (!rows.isEmpty()) {
                    indicatorValueObj = rows.get(0).get(valueField);
                }
                resp.setChartData(toNumberIfPossible(indicatorValueObj));
                break;

            case "pie":
            case "bar":
            case "line":
                String labelField = mapping.getOrDefault("label", "label");
                String valField = mapping.getOrDefault("value", "value");
                List<Map<String, Object>> data = new ArrayList<>();
                for (Map<String, Object> r : rows) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("label", r.get(labelField));
                    item.put("value", toNumberIfPossible(r.get(valField)));
                    data.add(item);
                }
                resp.setChartData(data);
                break;

            default:
                resp.setChartData(rows);
                break;
        }

        return resp;
    }


    /**
     * 根据查询结果和图表类型智能推断列映射
     */
    private Map<String, String> smartMapColumns(List<Map<String, Object>> rows, ChartTypeEntity chartType) {
        Map<String, String> inferredMapping = new HashMap<>();
        if (rows.isEmpty()) {
            return inferredMapping;
        }

        Map<String, Object> firstRow = rows.get(0);
        List<String> availableColumns = new ArrayList<>(firstRow.keySet());

        // 智能映射逻辑
        switch (chartType.getTypeCode()) {
            case "indicator":
                // 找到第一个数值型列作为 value
                for (String col : availableColumns) {
                    Object value = firstRow.get(col);
                    if (value instanceof Number || value instanceof BigDecimal) {
                        inferredMapping.put("value", col);
                        return inferredMapping;
                    }
                }
                // 如果没有找到数值型，则使用第一个列作为 fallback
                if (!availableColumns.isEmpty()) {
                    inferredMapping.put("value", availableColumns.get(0));
                }
                break;

            case "pie":
            case "bar":
            case "line":
                String labelColumn = null;
                String valueColumn = null;

                // 找到第一个字符串型列作为 label，第一个数值型列作为 value
                for (String col : availableColumns) {
                    Object value = firstRow.get(col);
                    if (labelColumn == null && value instanceof String) {
                        labelColumn = col;
                    } else if (valueColumn == null && (value instanceof Number || value instanceof BigDecimal)) {
                        valueColumn = col;
                    }
                    // 如果都找到了，则停止循环
                    if (labelColumn != null && valueColumn != null) {
                        break;
                    }
                }

                if (labelColumn != null) {
                    inferredMapping.put("label", labelColumn);
                }
                if (valueColumn != null) {
                    inferredMapping.put("value", valueColumn);
                }
                break;
        }

        return inferredMapping;
    }

    private Number toNumberIfPossible(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return (Number) v;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof String) {
            try {
                String s = ((String) v).trim();
                if (s.contains(".")) return Double.parseDouble(s);
                else return Long.parseLong(s);
            } catch (Exception ignore) {
            }
        }
        return null;
    }
}