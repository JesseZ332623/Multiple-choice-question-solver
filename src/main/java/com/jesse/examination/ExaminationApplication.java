package com.jesse.examination;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Boot 应用启动类。
 *
 * <ul>
 *     <li>EnableRedisHttpSession 注解用于启用 Http 会话。</li>
 *     <li>EnableScheduling       注解用于启用定时任务执行</li>
 * </ul>
 */
@EnableScheduling
@EnableRedisHttpSession
@SpringBootApplication
public class ExaminationApplication
{
	public static void main(String[] args) {
		SpringApplication.run(ExaminationApplication.class, args);
	}
}
