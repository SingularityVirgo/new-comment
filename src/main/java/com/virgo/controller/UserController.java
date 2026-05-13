package com.virgo.controller;

import com.virgo.domain.dto.auth.LoginRequest;
import com.virgo.domain.dto.user.NickNameUpdateCommand;
import com.virgo.domain.dto.user.PasswordChangeCommand;
import com.virgo.domain.dto.userinfo.UserInfoUpdateCommand;
import com.virgo.web.api.Result;
import com.virgo.service.IUserInfoService;
import com.virgo.service.IUserService;
import com.virgo.web.assembly.WebModels;
import jakarta.servlet.http.HttpServletRequest;
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
    public Result<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        return Result.ok(userService.login(loginRequest, session));
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        userService.logout(request.getHeader("authorization"));
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<?> me() {
        return Result.ok(userService.getMyAccount());
    }

    @PostMapping("/me/code")
    public Result<?> sendMyCode(HttpSession session) {
        userService.sendCodeToMyPhone(session);
        return Result.ok();
    }

    @PutMapping("/me/nickname")
    public Result<?> updateNickname(@RequestBody NickNameUpdateCommand command) {
        userService.updateMyNickName(command);
        return Result.ok();
    }

    @PutMapping("/me/password")
    public Result<?> changePassword(@RequestBody PasswordChangeCommand command, HttpSession session) {
        userService.changeMyPassword(command, session);
        return Result.ok();
    }

    @GetMapping("/info/{id}")
    public Result<?> info(@PathVariable("id") Long userId) {
        return Result.ok(userInfoService.findPresentableInfo(userId)
                .map(WebModels::toUserInfoVo)
                .orElse(null));
    }

    @PutMapping("/info")
    public Result<?> updateMyInfo(@RequestBody UserInfoUpdateCommand command) {
        userInfoService.updateMyProfile(command);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<?> queryUserById(@PathVariable("id") Long userId) {
        return Result.ok(userService.findUserProfile(userId)
                .map(WebModels::toUserProfileVo)
                .orElse(null));
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
