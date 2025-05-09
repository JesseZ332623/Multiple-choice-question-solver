package com.jesse.examination.config.websecurityconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesse.examination.user.dto.userdto.UserLoginDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Json 认证筛选器（暂时没有启用）。
 */
public class JsonAuthenticationFilter extends UsernamePasswordAuthenticationFilter
{
    @Override
    public Authentication
    attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            // 从请求体中解析JSON
            UserLoginDTO loginRequest
                    = new ObjectMapper().readValue(request.getInputStream(), UserLoginDTO.class);

            String username = loginRequest.getUserName();
            String password = loginRequest.getPassword();

            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

            return this.getAuthenticationManager().authenticate(authRequest);
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }
}