package org.spirodelaz.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("query_chart_mapping")
public class QueryChartMappingEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long queryId;
    private String chartTypeCode;
    private String columnMappings; // JSON string like {"label":"name","value":"amount"}
    private Boolean isDefault;
}

