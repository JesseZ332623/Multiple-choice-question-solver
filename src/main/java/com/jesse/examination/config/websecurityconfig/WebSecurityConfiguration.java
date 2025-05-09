package com.jesse.examination.config.websecurityconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring 网络安全配置类（遇到一些阻碍，暂时停用）。
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration
{
    // 所有人都可以访问的 URL
    private static final String[] PUBLIC_URLS = {
            "/",
            "/user_info/login",
            "/user_info/register",
            "/api/email/**"
    };

    // 管理员或仅拥有用户权限的人可访问的 URL
    private static final String[] USERS_PERMIT_URLS = {
            "/api/question/**",
            "/api/redis/**",
            "/api/score_record/**",
            "/api/user_info/**",
            "/question/**",
            "/score_record/**",
            "/user_info/**",
            "/error/**"
    };

    // 管理员可访问的 URL
    private static final String[] ADMIN_PERMIT_URLS = {
            "/api/question/**",
            "/api/email/**",
            "/api/redis/**",
            "/api/score_record/**",
            "/api/user_info/**",
            "/api/admin/**",
            "/question/**",
            "/score_record/**",
            "/user_info/**",
            "/admin/**",
            "/error/**",
            "/manage/**"
    };

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain
    filterChain(HttpSecurity http) throws Exception
    {
//         http.addFilterBefore(
//                 new JsonAuthenticationFilter(),
//                 UsernamePasswordAuthenticationFilter.class
//         );
//
//        // 使用 Cookie 存储 CSRF 令牌
//        http.csrf(csrf -> csrf
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                );
//
//         http.authorizeHttpRequests(
//                (auth) ->
//                        auth.requestMatchers(PUBLIC_URLS).permitAll()
//                            .requestMatchers(USERS_PERMIT_URLS).hasAnyRole("USER", "ADMIN")
//                            .requestMatchers(ADMIN_PERMIT_URLS).hasRole("ADMIN")
//                            .anyRequest().authenticated()
//        ).formLogin(
//                (form) ->
//                        form.loginPage("/user_info/login")
//                            .loginProcessingUrl("/api/user_info/login")             // 指定登录表单提交的URL
//                            .defaultSuccessUrl("/user_info/user_front_page", true)
//                            .permitAll()
//         ).exceptionHandling(
//                 (handle) ->
//                         handle.accessDeniedPage("/error/access_denied")
//         );

        http.authorizeHttpRequests(
                (auth) ->
                        auth.requestMatchers("/**").permitAll()
        );

        return http.build();
    }
}
