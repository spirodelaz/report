package org.spirodelaz.report.util;

import java.util.*;

public class ResultMappingUtils {

    /**
     * 将查询结果根据列映射规则转换为标准格式
     *
     * @param rows 原始SQL结果，例如 [{"pname":"iPhone","total_sales":1000}, ...]
     * @param mapping 列映射规则，例如 {"label":"pname","value":"total_sales"}
     * @return 转换后的结果，例如 [{"label":"iPhone","value":1000}, ...]
     */
    public static List<Map<String, Object>> applyMapping(List<Map<String, Object>> rows,
                                                         Map<String, String> mapping) {
        List<Map<String, Object>> mappedResults = new ArrayList<>();

        if (rows == null || rows.isEmpty()) {
            return mappedResults;
        }

        for (Map<String, Object> row : rows) {
            Map<String, Object> mappedRow = new LinkedHashMap<>();

            // 遍历映射规则
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                String targetField = entry.getKey();   // 目标字段 (label/value)
                String sourceField = entry.getValue(); // SQL原始字段 (pname/total_sales)

                if (row.containsKey(sourceField)) {
                    mappedRow.put(targetField, row.get(sourceField));
                } else {
                    mappedRow.put(targetField, null); // 避免丢字段
                }
            }

            mappedResults.add(mappedRow);
        }

        return mappedResults;
    }
}
