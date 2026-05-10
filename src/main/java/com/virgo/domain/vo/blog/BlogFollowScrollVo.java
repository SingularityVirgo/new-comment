package com.virgo.domain.vo.blog;

import lombok.Data;

import java.util.List;

/** 关注动态流分页结果（接口响应） */
@Data
public class BlogFollowScrollVo {
    private List<BlogFeedVo> list;
    private Long minTime;
    private Integer offset;
}
