package org.spirodelaz.report.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExecuteReportResponse {

    /**
     * 列信息（元数据）
     */
    private List<ColumnMeta> columns;

    /**
     * 数据值，每一行对应一个 List<CellValue>
     */
    private List<List<CellValue>> values;

    @Data
    public static class ColumnMeta {
        private String column;      // 列名（如 product_name, total_amount）
    }

    @Data
    public static class CellValue {
        private Object value;     // 单元格值
    }
}
