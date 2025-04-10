//package com.jesse.examination.configuration;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authorization.AuthorizationDecision;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
//import org.springframework.security.web.util.matcher.RequestMatcher;
//import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
//
//@Configuration
//@EnableWebSecurity
//public class WebSecurityConfig
//{
//    @Bean
//    public SecurityFilterChain securityFilterChain(
//            HttpSecurity httpSecurity,
//            HandlerMappingIntrospector introspector) throws Exception {
//
//        // 放行所有请求（包括跳转）
//        httpSecurity
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll()
//                );
//
//        return httpSecurity.build();
//    }
//}
