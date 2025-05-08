package com.jesse.examination.config.websecurityconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring 网络安全配置类。
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration
{
    // 所有人都可以访问的 URL
    private static final String[] PUBLIC_URLS = {
            "/user_info/login",
            "/user_info/register"
    };

    // 管理员或仅拥有用户权限的人可访问的 URL
    private static final String[] USERS_PERMIT_URLS = {
            "/api/question/**",
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
            "/api/score_record/**",
            "/api/user_info/**",
            "/api/admin/**",
            "/question/**",
            "/score_record/**",
            "/user_info/**"
    };

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
//         http.authorizeHttpRequests(
//                (auth) ->
//                        auth.requestMatchers(PUBLIC_URLS).permitAll()
//                            .requestMatchers(USERS_PERMIT_URLS).hasAnyRole("USER", "ADMIN")
//                            .requestMatchers(ADMIN_PERMIT_URLS).hasRole("ADMIN")
//                            .anyRequest().authenticated()
//        ).formLogin(
//                (form) ->
//                        form.loginPage("/user_info/login")
//                            .permitAll()
//         ).exceptionHandling(
//                 (handle) ->
//                         handle.accessDeniedPage("/error/access_denied")
//         );

        http.authorizeHttpRequests((auth) ->
                auth.requestMatchers("/**").permitAll()
        ); //.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
