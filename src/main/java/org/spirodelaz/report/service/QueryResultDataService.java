package org.spirodelaz.report.service;

import org.spirodelaz.report.entity.QueryResultDataEntity;

public interface QueryResultDataService {
    // void saveResult(Long queryId, String resultJson);
    QueryResultDataEntity saveOrUpdateResult(Long queryId, String resultJson);
}
