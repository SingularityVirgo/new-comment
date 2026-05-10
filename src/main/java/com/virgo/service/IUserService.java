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

    void sign();

    Integer signCount();

    Optional<UserProfileDto> findUserProfile(Long userId);
}
