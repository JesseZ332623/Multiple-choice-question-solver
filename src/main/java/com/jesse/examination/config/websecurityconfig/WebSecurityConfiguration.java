package com.jesse.examination.config.websecurityconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration
{
    private static final String[] PUBLIC_URLS = {
            "/user_info/login",
            "/user_info/register"
    };

    private static final String[] USERS_PERMIT_URLS = {
            "/api/question/**",
            "/api/score_record/**",
            "/api/user_info/**",
            "/question/**",
            "/score_record/**",
            "/user_info/**",
            "/error/**"
    };

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
        );// .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
