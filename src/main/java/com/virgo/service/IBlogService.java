package com.virgo.service;

import com.virgo.dto.ScrollResult;
import com.virgo.dto.UserDTO;
import com.virgo.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IBlogService extends IService<Blog> {

    List<Blog> queryHotBlog(Integer current);

    Blog queryBlogById(Long id);

    void likeBlog(Long id);

    List<UserDTO> queryBlogLikes(Long id);

    Long saveBlog(Blog blog);

    ScrollResult queryBlogOfFollow(Long max, Integer offset);

    List<Blog> pageBlogsForCurrentUser(Integer current);

    List<Blog> pageBlogsForUser(Long userId, Integer current);

    void updateMyBlog(Blog blog);

    void removeMyBlog(Long id);
}
