package org.spirodelaz.report.controller;

import org.spirodelaz.report.dto.ExecuteReportRequest;
import org.spirodelaz.report.dto.ExecuteReportResponse;
import org.spirodelaz.report.service.ReportExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportExecutionService reportExecutionService;

    public ReportController(ReportExecutionService reportExecutionService) {
        this.reportExecutionService = reportExecutionService;
    }

    @PostMapping("/execute")
    public ResponseEntity<ExecuteReportResponse> execute(@RequestBody ExecuteReportRequest request) {
        ExecuteReportResponse response = reportExecutionService.execute(request);
        return ResponseEntity.ok(response);
    }
}

