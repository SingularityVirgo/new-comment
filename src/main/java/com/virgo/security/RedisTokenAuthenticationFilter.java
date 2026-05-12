package com.virgo.security;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.virgo.domain.dto.auth.CurrentUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.virgo.utils.RedisConstants.LOGIN_USER_KEY;
import static com.virgo.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * 从请求头读取与历史一致的 token，在 Redis 中解析会话并写入 {@link SecurityContextHolder}。
 */
@Component
@RequiredArgsConstructor
public class RedisTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final List<SimpleGrantedAuthority> USER_AUTHORITIES = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = request.getHeader("authorization");
            if (StrUtil.isNotBlank(token)) {
                String key = LOGIN_USER_KEY + token;
                Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
                if (!entries.isEmpty()) {
                    CurrentUser currentUser = BeanUtil.fillBeanWithMap(entries, new CurrentUser(), false);
                    stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(currentUser, token, USER_AUTHORITIES);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
