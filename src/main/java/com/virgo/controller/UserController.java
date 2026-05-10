package com.virgo.controller;

import com.virgo.dto.LoginFormDTO;
import com.virgo.dto.Result;
import com.virgo.entity.UserInfo;
import com.virgo.service.IUserInfoService;
import com.virgo.service.IUserService;
import com.virgo.utils.UserHolder;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final IUserInfoService userInfoService;

    @PostMapping("code")
    public Result<?> sendCode(@RequestParam("phone") String phone, HttpSession session) {
        userService.sendCode(phone, session);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginFormDTO loginForm, HttpSession session) {
        return Result.ok(userService.login(loginForm, session));
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        UserHolder.removeUser();
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<?> me() {
        return Result.ok(UserHolder.getUser());
    }

    @GetMapping("/info/{id}")
    public Result<?> info(@PathVariable("id") Long userId) {
        return Result.ok(userInfoService.findPresentableInfo(userId).orElse(null));
    }

    @PutMapping("/info")
    public Result<?> updateMyInfo(@RequestBody UserInfo info) {
        userInfoService.updateMyProfile(info);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<?> queryUserById(@PathVariable("id") Long userId) {
        return Result.ok(userService.findUserDtoById(userId).orElse(null));
    }

    @PostMapping("/sign")
    public Result<?> sign() {
        userService.sign();
        return Result.ok();
    }

    @GetMapping("/sign/count")
    public Result<?> signCount() {
        return Result.ok(userService.signCount());
    }
}
