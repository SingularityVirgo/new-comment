package com.virgo.domain.dto.auth;

import lombok.Data;

/**
 * 登录：二选一
 * <ul>
 *   <li>手机号 + 短信验证码：填写 {@code code}</li>
 *   <li>手机号 + 密码：填写 {@code password}</li>
 * </ul>
 */
@Data
public class LoginRequest {
    private String phone;
    /** 验证码登录 */
    private String code;
    /** 密码登录 */
    private String password;
}
