package com.virgo.domain.vo.follow;

import lombok.Data;

/** 关注列表中的用户（接口响应） */
@Data
public class FollowingUserVo {
    private Long id;
    private String nickName;
    private String icon;
    private Boolean isFollow;
}
