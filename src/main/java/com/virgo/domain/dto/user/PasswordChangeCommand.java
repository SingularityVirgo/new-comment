package com.virgo.domain.dto.user;

import lombok.Data;

@Data
public class PasswordChangeCommand {
    private String newPassword;
    /** 下发至绑定手机号的短信验证码 */
    private String code;
}
