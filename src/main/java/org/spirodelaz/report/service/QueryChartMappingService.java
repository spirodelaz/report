package org.spirodelaz.report.service;

import org.spirodelaz.report.entity.QueryChartMappingEntity;

import java.util.Optional;

public interface QueryChartMappingService {
    Optional<QueryChartMappingEntity> findByQueryIdAndChartTypeCode(Long queryId, String chartTypeCode);
}

