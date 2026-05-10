package com.virgo.domain.dto.blog;

import lombok.Data;

import java.util.List;

/** 关注动态流分页结果（服务层） */
@Data
public class BlogFollowScrollDto {
    private List<BlogFeedDto> list;
    private Long minTime;
    private Integer offset;
}
