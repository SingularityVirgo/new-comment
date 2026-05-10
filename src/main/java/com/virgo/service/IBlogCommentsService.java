package com.virgo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virgo.domain.dto.comment.BlogCommentItemDto;
import com.virgo.domain.dto.comment.CommentCreateCommand;
import com.virgo.domain.po.BlogComments;

import java.util.List;

public interface IBlogCommentsService extends IService<BlogComments> {

    Long addComment(CommentCreateCommand command);

    List<BlogCommentItemDto> pageForBlog(Long blogId, Integer current);

    void updateMyComment(Long id, String content);

    void removeMyComment(Long id);
}
