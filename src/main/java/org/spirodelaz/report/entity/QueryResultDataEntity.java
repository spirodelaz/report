package org.spirodelaz.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("query_result_data")
public class QueryResultDataEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long queryId;
    private String resultData;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
}

