package com.jesse.examination;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Boot 应用启动类。
 *
 * <ul>
 *     <li>EnableAsync			  注解用于启用异步任务的执行</li>
 *     <li>EnableScheduling       注解用于启用定时任务执行</li>
 * </ul>
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class ExaminationApplication
{
	public static void main(String[] args) {
		SpringApplication.run(ExaminationApplication.class, args);
	}
}
