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
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisTemplate 配置。
 */
@Configuration
public class RedisConfig
{
    /**
     * Redis 连接工厂配置。
     *
     * @param host      主机 IP
     * @param port      端口号
     * @param password  密码
     */
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

    /**
     * 使用 Jackson 库而非 JDK 默认的序列化。
     */
    @Bean
    public RedisTemplate<String, Object>
    configRedisTemplate(
            @Qualifier(value = "redisConnectionFactory")
            RedisConnectionFactory connectionFactory
    )
    {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 键采用字符串序列化
        template.setKeySerializer(new StringRedisSerializer());

        // 值采用泛型序列化
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}
