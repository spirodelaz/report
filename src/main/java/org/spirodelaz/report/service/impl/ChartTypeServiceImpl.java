package org.spirodelaz.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.spirodelaz.report.entity.ChartTypeEntity;
import org.spirodelaz.report.mapper.ChartTypeMapper;
import org.spirodelaz.report.service.ChartTypeService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChartTypeServiceImpl implements ChartTypeService {

    private final ChartTypeMapper mapper;

    public ChartTypeServiceImpl(ChartTypeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ChartTypeEntity> findByCode(String code) {
        return mapper.selectList(new QueryWrapper<ChartTypeEntity>().eq("type_code", code))
                .stream().findFirst();
    }
}

