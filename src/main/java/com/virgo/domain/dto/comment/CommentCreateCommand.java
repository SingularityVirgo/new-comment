package com.virgo.domain.dto.comment;

import lombok.Data;

/** 发表评论 */
@Data
public class CommentCreateCommand {
    private Long blogId;
    private Long parentId;
    private Long answerId;
    private String content;
}
