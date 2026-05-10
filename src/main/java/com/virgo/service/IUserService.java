package com.virgo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virgo.dto.LoginFormDTO;
import com.virgo.dto.Result;
import com.virgo.entity.User;
import jakarta.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();
}
