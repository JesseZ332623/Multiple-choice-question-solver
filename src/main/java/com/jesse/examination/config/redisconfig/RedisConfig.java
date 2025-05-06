package com.jesse.examination.config.redisconfig;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisTemplate 配置，使用 Jackson 库而非 JDK 默认的序列化。
 */
@Configuration
public class RedisConfig
{
    @Bean
    public RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.redis.host}")      String host,
            @Value("${spring.redis.port}")      int port,
            @Value("${spring.redis.password}")  String password
    )
    {
        var conf = new RedisStandaloneConfiguration(host, port);

        conf.setPassword(password);

        return new LettuceConnectionFactory(conf);
    }

    @Bean
    public RedisTemplate<String, Object>
    configRedisTemplate(
            @Qualifier(value = "redisConnectionFactory")
            RedisConnectionFactory connectionFactory
    )
    {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用 Jackson2JsonRedisSerializer 序列化值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}
