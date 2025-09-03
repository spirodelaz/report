package org.spirodelaz.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.spirodelaz.report.entity.QueryResultDataEntity;
import org.spirodelaz.report.mapper.QueryResultDataMapper;
import org.spirodelaz.report.service.QueryResultDataService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class QueryResultDataServiceImpl implements QueryResultDataService {

    private final QueryResultDataMapper queryResultDataMapper;

    public QueryResultDataServiceImpl(QueryResultDataMapper queryResultDataMapper) {
        this.queryResultDataMapper = queryResultDataMapper;
    }

//    @Override
//    public void saveResult(Long queryId, String resultJson) {
//        QueryResultDataEntity entity = new QueryResultDataEntity();
//        entity.setQueryId(queryId);
//        entity.setResultData(resultJson);
//        entity.setCreatedAt(LocalDateTime.now());
//        entity.setExpireAt(LocalDateTime.now().plusHours(1)); // 1小时后过期，可根据需求调整
//        mapper.insert(entity);
//    }

    @Override
    public QueryResultDataEntity saveOrUpdateResult(Long queryId, String resultJson) {
        // Find existing record by queryId
        QueryResultDataEntity entity = queryResultDataMapper.selectOne(
                new QueryWrapper<QueryResultDataEntity>().eq("query_id", queryId)
        );

        if (entity == null) {
            entity = new QueryResultDataEntity();
            entity.setQueryId(queryId);
            entity.setCreatedAt(LocalDateTime.now());
        }

        entity.setResultData(resultJson);
        entity.setExpireAt(LocalDateTime.now().plusHours(1));
        entity.setCreatedAt(LocalDateTime.now());
        if (entity.getId() == null) {
            queryResultDataMapper.insert(entity);
        } else {
            queryResultDataMapper.updateById(entity);
        }

        return entity;
    }
}

