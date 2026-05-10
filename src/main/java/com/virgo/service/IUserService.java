package com.virgo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virgo.dto.LoginFormDTO;
import com.virgo.dto.UserDTO;
import com.virgo.entity.User;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

public interface IUserService extends IService<User> {

    void sendCode(String phone, HttpSession session);

    String login(LoginFormDTO loginForm, HttpSession session);

    void sign();

    Integer signCount();

    Optional<UserDTO> findUserDtoById(Long userId);
}
