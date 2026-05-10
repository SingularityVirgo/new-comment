package com.virgo.domain.dto.blog;

import lombok.Data;

/** 更新笔记（写请求，字段均可选，按非空更新） */
@Data
public class BlogUpdateCommand {
    private Long id;
    private Long shopId;
    private String title;
    private String images;
    private String content;
}
