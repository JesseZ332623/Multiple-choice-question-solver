package com.jesse.examination.config.jacksonconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JacksonConfig
{
    public static ObjectMapper createObjectMapper()
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // 注册JavaTimeModule

        return mapper;
    }
}
