package org.spirodelaz.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chart_type")
public class ChartTypeEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String typeCode;
    private String typeName;
    private String requiredFields; // JSON string
    private String defaultMappingTemplate; // JSON string
    private LocalDateTime createdAt;
}

