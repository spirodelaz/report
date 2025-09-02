package org.spirodelaz.report.service;


import org.spirodelaz.report.entity.QueryDefinitionEntity;

import java.util.Optional;

public interface QueryDefinitionService {
    Optional<QueryDefinitionEntity> findById(Long id);
    Optional<QueryDefinitionEntity> findBySqlText(String sqlText);
    QueryDefinitionEntity save(QueryDefinitionEntity entity);
}

