package com.virgo.domain.vo.user;

import lombok.Data;

/** 用户基础资料（接口响应） */
@Data
public class UserProfileVo {
    private Long id;
    private String nickName;
    private String icon;
}
