package com.virgo.domain.vo.blog;

import lombok.Data;

import java.time.LocalDateTime;

/** 笔记列表/详情（接口响应） */
@Data
public class BlogFeedVo {
    private Long id;
    private Long shopId;
    private Long userId;
    private String icon;
    private String name;
    private Boolean isLike;
    private String title;
    private String images;
    private String content;
    private Integer liked;
    private Integer comments;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
