package com.virgo.domain.dto.blog;

import lombok.Data;

/** 发布笔记（写请求） */
@Data
public class BlogSaveCommand {
    private Long shopId;
    private String title;
    private String images;
    private String content;
}
