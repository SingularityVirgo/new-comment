package com.virgo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virgo.domain.dto.blog.BlogFeedDto;
import com.virgo.domain.dto.blog.BlogFollowScrollDto;
import com.virgo.domain.dto.blog.BlogLikeUserDto;
import com.virgo.domain.dto.blog.BlogSaveCommand;
import com.virgo.domain.dto.blog.BlogUpdateCommand;
import com.virgo.domain.po.Blog;

import java.util.List;

public interface IBlogService extends IService<Blog> {

    List<BlogFeedDto> queryHotBlog(Integer current);

    BlogFeedDto queryBlogById(Long id);

    void likeBlog(Long id);

    List<BlogLikeUserDto> queryBlogLikes(Long id);

    Long saveBlog(BlogSaveCommand command);

    BlogFollowScrollDto queryBlogOfFollow(Long max, Integer offset);

    List<BlogFeedDto> pageBlogsForCurrentUser(Integer current);

    List<BlogFeedDto> pageBlogsForUser(Long userId, Integer current);

    void updateMyBlog(BlogUpdateCommand command);

    void removeMyBlog(Long id);
}
