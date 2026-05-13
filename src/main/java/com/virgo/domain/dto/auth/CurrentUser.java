package com.virgo.domain.dto.auth;

import lombok.Data;

/**
 * 当前登录用户会话信息（Redis / ThreadLocal），仅含鉴权与展示所需字段。
 */
@Data
public class CurrentUser {
    private Long id;
    /** 完整手机号，仅服务端会话使用；对外展示请脱敏 */
    private String phone;
    private String nickName;
    private String icon;
}
