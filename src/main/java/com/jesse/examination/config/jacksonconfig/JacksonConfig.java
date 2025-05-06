package com.jesse.examination.config.jacksonconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 原生的 Jackson 库不支持对时间字符串（如 ISO 标准格式：2025-04-30T09:41:01）的转化，
 * 所以需要一个工厂方法去构造注册了 JavaTime Module 的 ObjectMapper。
 */
public class JacksonConfig
{
    public static ObjectMapper createObjectMapper()
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // 注册 JavaTime Module

        return mapper;
    }
}
