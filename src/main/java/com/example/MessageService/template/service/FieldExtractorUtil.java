package com.example.MessageService.template.service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FieldExtractorUtil {
    public static Map<String, String> extractFieldsAsMap(Object obj) {
        Map<String, String> map = new HashMap<>();

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    map.put(field.getName(), value.toString());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + field.getName(), e);
            }
        }

        return map;
    }
}
