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

    private final QueryResultDataMapper mapper;

    public QueryResultDataServiceImpl(QueryResultDataMapper mapper) {
        this.mapper = mapper;
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
        Optional<QueryResultDataEntity> existingResult = mapper.selectList(
                new QueryWrapper<QueryResultDataEntity>().eq("query_id", queryId)
        ).stream().findFirst();

        QueryResultDataEntity entity;
        if (existingResult.isPresent()) {
            // If exists, update it
            entity = existingResult.get();
            entity.setResultData(resultJson);
            entity.setCreatedAt(LocalDateTime.now());
            // You can also add logic to update expire_at here if you have that field.
            mapper.updateById(entity);
        } else {
            // If it doesn't exist, insert a new record
            entity = new QueryResultDataEntity();
            entity.setQueryId(queryId);
            entity.setResultData(resultJson);
            entity.setCreatedAt(LocalDateTime.now());
            // Set expire_at based on your business logic, for example:
            // entity.setExpireAt(LocalDateTime.now().plusHours(1));
            mapper.insert(entity);
        }

        return entity;
    }
}

