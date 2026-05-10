package com.virgo.domain.vo.blog;

import lombok.Data;

/** 点赞用户简要信息（接口响应） */
@Data
public class BlogLikeUserVo {
    private Long id;
    private String nickName;
    private String icon;
}
