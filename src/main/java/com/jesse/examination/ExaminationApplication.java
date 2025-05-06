package com.jesse.examination;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication

/*
 * EnableRedisHttpSession 注解用于启用 Http 会话。
 */
@EnableRedisHttpSession
public class ExaminationApplication
{
	public static void main(String[] args) {
		SpringApplication.run(ExaminationApplication.class, args);
	}
}
