package com.virgo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virgo.common.exception.BizException;
import com.virgo.dto.ScrollResult;
import com.virgo.dto.UserDTO;
import com.virgo.entity.Blog;
import com.virgo.entity.BlogComments;
import com.virgo.entity.Follow;
import com.virgo.entity.User;
import com.virgo.mapper.BlogCommentsMapper;
import com.virgo.mapper.BlogMapper;
import com.virgo.service.IBlogService;
import com.virgo.service.IFollowService;
import com.virgo.service.IUserService;
import com.virgo.utils.SystemConstants;
import com.virgo.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.virgo.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.virgo.utils.RedisConstants.FEED_KEY;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    private final IUserService userService;
    private final StringRedisTemplate stringRedisTemplate;
    private final IFollowService followService;
    private final BlogCommentsMapper blogCommentsMapper;

    @Override
    public List<Blog> queryHotBlog(Integer current) {
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        records.forEach(blog -> {
            queryBlogUser(blog);
            isBlogLiked(blog);
        });
        return records;
    }

    @Override
    public Blog queryBlogById(Long id) {
        Blog blog = getById(id);
        if (blog == null) {
            throw new BizException("笔记不存在！");
        }
        queryBlogUser(blog);
        isBlogLiked(blog);
        return blog;
    }

    private void isBlogLiked(Blog blog) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return;
        }
        String key = BLOG_LIKED_KEY + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, user.getId().toString());
        blog.setIsLike(score != null);
    }

    @Override
    public void likeBlog(Long id) {
        Long userId = UserHolder.getUser().getId();
        String key = BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null) {
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
    }

    @Override
    public List<UserDTO> queryBlogLikes(Long id) {
        String key = BLOG_LIKED_KEY + id;
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);
        return userService.query()
                .in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Long saveBlog(Blog blog) {
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        if (!save(blog)) {
            throw new BizException("新增笔记失败!");
        }
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        for (Follow follow : follows) {
            Long fanId = follow.getUserId();
            String key = FEED_KEY + fanId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }
        return blog.getId();
    }

    @Override
    public ScrollResult queryBlogOfFollow(Long max, Integer offset) {
        Long userId = UserHolder.getUser().getId();
        String key = FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);
        if (typedTuples == null || typedTuples.isEmpty()) {
            ScrollResult empty = new ScrollResult();
            empty.setList(Collections.emptyList());
            empty.setMinTime(0L);
            empty.setOffset(0);
            return empty;
        }
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            ids.add(Long.valueOf(tuple.getValue()));
            long time = tuple.getScore().longValue();
            if (time == minTime) {
                os++;
            } else {
                minTime = time;
                os = 1;
            }
        }
        String idStr = StrUtil.join(",", ids);
        List<Blog> blogs = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Blog b : blogs) {
            queryBlogUser(b);
            isBlogLiked(b);
        }
        ScrollResult scrollResult = new ScrollResult();
        scrollResult.setList(blogs);
        scrollResult.setOffset(os);
        scrollResult.setMinTime(minTime);
        return scrollResult;
    }

    @Override
    public List<Blog> pageBlogsForCurrentUser(Integer current) {
        UserDTO user = UserHolder.getUser();
        Page<Blog> page = query()
                .eq("user_id", user.getId())
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        return page.getRecords();
    }

    @Override
    public List<Blog> pageBlogsForUser(Long userId, Integer current) {
        Page<Blog> page = query()
                .eq("user_id", userId)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        return page.getRecords();
    }

    @Override
    public void updateMyBlog(Blog blog) {
        if (blog.getId() == null) {
            throw new BizException("笔记id不能为空");
        }
        Long uid = UserHolder.getUser().getId();
        Blog existing = getById(blog.getId());
        if (existing == null) {
            throw new BizException("笔记不存在！");
        }
        if (!uid.equals(existing.getUserId())) {
            throw new BizException("无权修改该笔记");
        }
        LambdaUpdateWrapper<Blog> w = new LambdaUpdateWrapper<Blog>()
                .eq(Blog::getId, blog.getId())
                .eq(Blog::getUserId, uid);
        if (blog.getShopId() != null) {
            w.set(Blog::getShopId, blog.getShopId());
        }
        if (StrUtil.isNotBlank(blog.getTitle())) {
            w.set(Blog::getTitle, blog.getTitle());
        }
        if (StrUtil.isNotBlank(blog.getImages())) {
            w.set(Blog::getImages, blog.getImages());
        }
        if (StrUtil.isNotBlank(blog.getContent())) {
            w.set(Blog::getContent, blog.getContent());
        }
        if (!update(w)) {
            throw new BizException("更新笔记失败");
        }
    }

    @Override
    public void removeMyBlog(Long id) {
        Long uid = UserHolder.getUser().getId();
        Blog existing = getById(id);
        if (existing == null) {
            throw new BizException("笔记不存在！");
        }
        if (!uid.equals(existing.getUserId())) {
            throw new BizException("无权删除该笔记");
        }
        blogCommentsMapper.delete(new LambdaQueryWrapper<BlogComments>().eq(BlogComments::getBlogId, id));
        List<Follow> follows = followService.query().eq("follow_user_id", existing.getUserId()).list();
        for (Follow follow : follows) {
            String key = FEED_KEY + follow.getUserId();
            stringRedisTemplate.opsForZSet().remove(key, id.toString());
        }
        stringRedisTemplate.delete(BLOG_LIKED_KEY + id);
        if (!removeById(id)) {
            throw new BizException("删除笔记失败");
        }
    }

    private void queryBlogUser(Blog blog) {
        Long uid = blog.getUserId();
        User user = userService.getById(uid);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }
}
