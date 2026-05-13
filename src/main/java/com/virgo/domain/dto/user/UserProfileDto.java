package com.virgo.domain.dto.user;

import lombok.Data;

import java.time.LocalDateTime;

/** 用户基础资料（服务层查询结果） */
@Data
public class UserProfileDto {
    private Long id;
    private String nickName;
    private String icon;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
