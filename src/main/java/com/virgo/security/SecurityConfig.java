package com.virgo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 使用 Spring Security 统一声明公开接口与鉴权规则，Redis token 由 {@link RedisTokenAuthenticationFilter} 解析。
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RedisTokenAuthenticationFilter redisTokenAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/user/code",
                                "/user/login",
                                "/blog/hot",
                                "/shop/**",
                                "/shop-type/**",
                                "/upload/**",
                                "/voucher/**",
                                "/error")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(new RestAuthenticationEntryPoint(objectMapper)))
                .addFilterBefore(redisTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
