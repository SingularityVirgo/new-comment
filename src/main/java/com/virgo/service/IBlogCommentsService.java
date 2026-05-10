package com.virgo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virgo.entity.BlogComments;

import java.util.List;

public interface IBlogCommentsService extends IService<BlogComments> {

    Long addComment(BlogComments comment);

    List<BlogComments> pageForBlog(Long blogId, Integer current);

    void updateMyComment(Long id, String content);

    void removeMyComment(Long id);
}
