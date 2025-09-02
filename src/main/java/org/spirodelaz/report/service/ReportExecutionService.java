package org.spirodelaz.report.service;


import org.spirodelaz.report.dto.ExecuteReportRequest;
import org.spirodelaz.report.dto.ExecuteReportResponse;

public interface ReportExecutionService {
    ExecuteReportResponse execute(ExecuteReportRequest request);
}
