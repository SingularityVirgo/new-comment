package com.virgo.domain.dto.blog;

import lombok.Data;

/** 点赞用户简要信息（服务层） */
@Data
public class BlogLikeUserDto {
    private Long id;
    private String nickName;
    private String icon;
}
