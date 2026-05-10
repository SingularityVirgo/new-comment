package com.virgo.domain.dto.follow;

import lombok.Data;

/** 关注列表中的用户（服务层，含当前访问者是否已关注） */
@Data
public class FollowingUserDto {
    private Long id;
    private String nickName;
    private String icon;
    private Boolean isFollow;
}
