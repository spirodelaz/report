package org.spirodelaz.report.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExecuteReportResponse {
    private String chartType;
//    private Number indicatorValue;
    private Object chartData;
    private List<Map<String, Object>> rawResult;
    private Map<String, String> usedMapping;
}

