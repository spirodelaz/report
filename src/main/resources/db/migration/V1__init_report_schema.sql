-- 创建表结构（假设已经连接到 report_db 数据库）

CREATE TABLE IF NOT EXISTS query_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) COMMENT '查询名称',
    description TEXT COMMENT '查询描述',
    sql_text TEXT NOT NULL COMMENT 'SQL查询语句',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chart_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_code VARCHAR(50) NOT NULL UNIQUE COMMENT '图表类型编码',
    type_name VARCHAR(100) NOT NULL COMMENT '图表类型名称',
    required_fields JSON NULL COMMENT '必要字段',
    default_mapping_template JSON NULL COMMENT '默认映射模板',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS query_chart_mapping (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    query_id BIGINT NOT NULL COMMENT '关联的查询定义',
    chart_type_code VARCHAR(50) NOT NULL COMMENT '图表类型编码',
    column_mappings JSON NOT NULL COMMENT '列映射规则',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否为默认映射',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_query_chart (query_id, chart_type_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS query_result_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    query_id BIGINT NOT NULL,
    result_data JSON NOT NULL COMMENT '查询结果数据',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expire_at TIMESTAMP NULL COMMENT '过期时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始化基础 chart_type
INSERT INTO chart_type (type_code, type_name, required_fields, default_mapping_template)
VALUES
    ('indicator', '指标看板', JSON_ARRAY('value'), JSON_OBJECT('value','value')),
    ('pie', '饼图', JSON_ARRAY('label','value'), JSON_OBJECT('label','label','value','value')),
    ('bar', '柱状图', JSON_ARRAY('label','value'), JSON_OBJECT('label','label','value','value')),
    ('line', '折线图', JSON_ARRAY('label','value'), JSON_OBJECT('label','label','value','value'))
    ON DUPLICATE KEY UPDATE type_name=VALUES(type_name);

-- 示例查询（可选）
INSERT INTO query_definition (id, name, sql_text) VALUES
    (1, '销售总额', 'SELECT SUM(amount) as total_amount FROM sales'),
    (2, '产品分布', 'SELECT product_name as name, SUM(amount) as amount FROM sales GROUP BY product_name')
    ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 示例映射
INSERT INTO query_chart_mapping (query_id, chart_type_code, column_mappings, is_default) VALUES
    (1, 'indicator', JSON_OBJECT('value','total_amount'), TRUE),
    (2, 'pie', JSON_OBJECT('label','name','value','amount'), TRUE)
    ON DUPLICATE KEY UPDATE column_mappings=VALUES(column_mappings);
