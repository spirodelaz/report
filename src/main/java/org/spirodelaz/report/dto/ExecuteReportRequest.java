package org.spirodelaz.report.dto;

import lombok.Data;

/**
 * 执行报表请求
 * 可传 sqlText，并指定 chartTypeCode（indicator/pie/bar/line）
 */
@Data
public class ExecuteReportRequest {
    private String sqlText;
    private String chartTypeCode;
}

