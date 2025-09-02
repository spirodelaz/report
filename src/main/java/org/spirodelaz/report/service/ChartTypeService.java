package org.spirodelaz.report.service;

import org.spirodelaz.report.entity.ChartTypeEntity;

import java.util.Optional;

public interface ChartTypeService {
    Optional<ChartTypeEntity> findByCode(String code);
}

