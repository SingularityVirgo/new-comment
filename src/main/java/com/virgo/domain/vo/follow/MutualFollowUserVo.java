package com.virgo.domain.vo.follow;

import lombok.Data;

/** 共同关注用户（接口响应） */
@Data
public class MutualFollowUserVo {
    private Long id;
    private String nickName;
    private String icon;
}
