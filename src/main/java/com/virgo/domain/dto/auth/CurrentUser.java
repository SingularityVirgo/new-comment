package com.virgo.domain.dto.auth;

import lombok.Data;

/**
 * 当前登录用户会话信息（Redis / ThreadLocal），仅含鉴权与展示所需字段。
 */
@Data
public class CurrentUser {
    private Long id;
    private String nickName;
    private String icon;
}
