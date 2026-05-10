package com.virgo.domain.dto.follow;

import lombok.Data;

/** 共同关注用户（服务层） */
@Data
public class MutualFollowUserDto {
    private Long id;
    private String nickName;
    private String icon;
}
