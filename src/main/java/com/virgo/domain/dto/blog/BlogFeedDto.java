package com.virgo.domain.dto.blog;

import lombok.Data;

import java.time.LocalDateTime;

/** 笔记列表/详情对外展示（服务层组装） */
@Data
public class BlogFeedDto {
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
