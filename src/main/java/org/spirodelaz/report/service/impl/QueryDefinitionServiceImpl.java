package org.spirodelaz.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.spirodelaz.report.entity.QueryDefinitionEntity;
import org.spirodelaz.report.mapper.QueryDefinitionMapper;
import org.spirodelaz.report.service.QueryDefinitionService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QueryDefinitionServiceImpl implements QueryDefinitionService {

    private final QueryDefinitionMapper mapper;

    public QueryDefinitionServiceImpl(QueryDefinitionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<QueryDefinitionEntity> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public Optional<QueryDefinitionEntity> findBySqlText(String sqlText) {
        return mapper.selectList(new QueryWrapper<QueryDefinitionEntity>().eq("sql_text", sqlText))
                .stream().findFirst();
    }

    @Override
    public QueryDefinitionEntity save(QueryDefinitionEntity entity) {
        // MyBatis-Plus insert method populates the ID of the entity object
        mapper.insert(entity);
        return entity;
    }
}

