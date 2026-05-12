package com.virgo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virgo.domain.dto.auth.LoginRequest;
import com.virgo.domain.dto.user.UserProfileDto;
import com.virgo.domain.po.User;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

public interface IUserService extends IService<User> {

    void sendCode(String phone, HttpSession session);

    String login(LoginRequest loginRequest, HttpSession session);

    /** 使 Redis 中的会话失效；SecurityContext 与 ThreadLocal 由过滤器在请求结束时清理。 */
    void logout(String authorizationHeaderToken);

    void sign();

    Integer signCount();

    Optional<UserProfileDto> findUserProfile(Long userId);
}
