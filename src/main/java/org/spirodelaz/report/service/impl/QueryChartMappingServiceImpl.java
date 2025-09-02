package org.spirodelaz.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.spirodelaz.report.entity.QueryChartMappingEntity;
import org.spirodelaz.report.mapper.QueryChartMappingMapper;
import org.spirodelaz.report.service.QueryChartMappingService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QueryChartMappingServiceImpl implements QueryChartMappingService {

    private final QueryChartMappingMapper mapper;

    public QueryChartMappingServiceImpl(QueryChartMappingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<QueryChartMappingEntity> findByQueryIdAndChartTypeCode(Long queryId, String chartTypeCode) {
        return mapper.selectList(new QueryWrapper<QueryChartMappingEntity>()
                        .eq("query_id", queryId)
                        .eq("chart_type_code", chartTypeCode))
                .stream().findFirst();
    }
}

