package com.virgo.domain.vo.comment;

import lombok.Data;

import java.time.LocalDateTime;

/** 评论列表项（接口响应） */
@Data
public class BlogCommentVo {
    private Long id;
    private Long userId;
    private String name;
    private String icon;
    private Long blogId;
    private Long parentId;
    private Long answerId;
    private String content;
    private Integer liked;
    private Boolean status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
