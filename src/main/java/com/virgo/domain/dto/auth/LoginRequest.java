package com.virgo.domain.dto.auth;

import lombok.Data;

/** 登录：手机号 + 短信验证码 */
@Data
public class LoginRequest {
    private String phone;
    private String code;
}
