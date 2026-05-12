package com.virgo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.virgo.web.api.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 未认证访问受保护资源时返回与业务一致的 JSON，便于前端统一处理。
 */
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Result.fail("\u672a\u767b\u5f55\u6216\u767b\u5f55\u5df2\u8fc7\u671f"));
    }
}
