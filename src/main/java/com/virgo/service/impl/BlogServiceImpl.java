package com.virgo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virgo.common.exception.BizException;
import com.virgo.domain.dto.auth.CurrentUser;
import com.virgo.domain.dto.blog.BlogFeedDto;
import com.virgo.domain.dto.blog.BlogFollowScrollDto;
import com.virgo.domain.dto.blog.BlogLikeUserDto;
import com.virgo.domain.dto.blog.BlogSaveCommand;
import com.virgo.domain.dto.blog.BlogUpdateCommand;
import com.virgo.domain.po.Blog;
import com.virgo.domain.po.BlogComments;
import com.virgo.domain.po.Follow;
import com.virgo.domain.po.User;
import com.virgo.mapper.BlogCommentsMapper;
import com.virgo.mapper.BlogMapper;
import com.virgo.service.IBlogService;
import com.virgo.service.IFollowService;
import com.virgo.service.IUserService;
import com.virgo.utils.SystemConstants;
import com.virgo.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public List<BlogFeedDto> queryHotBlog(Integer current) {
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        enrichBlogs(records);
        return records.stream().map(this::toFeedDto).collect(Collectors.toList());
    }

    @Override
    public BlogFeedDto queryBlogById(Long id) {
        Blog blog = getById(id);
        if (blog == null) {
            throw new BizException("\u7b14\u8bb0\u4e0d\u5b58\u5728\uff01");
        }
        enrichBlog(blog);
        return toFeedDto(blog);
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
    public List<BlogLikeUserDto> queryBlogLikes(Long id) {
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
                .map(user -> BeanUtil.copyProperties(user, BlogLikeUserDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public Long saveBlog(BlogSaveCommand command) {
        CurrentUser user = UserHolder.getUser();
        Blog blog = new Blog();
        blog.setShopId(command.getShopId());
        blog.setTitle(command.getTitle());
        blog.setImages(command.getImages());
        blog.setContent(command.getContent());
        blog.setUserId(user.getId());
        if (!save(blog)) {
            throw new BizException("\u65b0\u589e\u7b14\u8bb0\u5931\u8d25\uff01");
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
    public BlogFollowScrollDto queryBlogOfFollow(Long max, Integer offset) {
        Long userId = UserHolder.getUser().getId();
        String key = FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);
        if (typedTuples == null || typedTuples.isEmpty()) {
            BlogFollowScrollDto empty = new BlogFollowScrollDto();
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
        enrichBlogs(blogs);
        BlogFollowScrollDto scrollResult = new BlogFollowScrollDto();
        scrollResult.setList(blogs.stream().map(this::toFeedDto).collect(Collectors.toList()));
        scrollResult.setOffset(os);
        scrollResult.setMinTime(minTime);
        return scrollResult;
    }

    @Override
    public List<BlogFeedDto> pageBlogsForCurrentUser(Integer current) {
        CurrentUser user = UserHolder.getUser();
        Page<Blog> page = query()
                .eq("user_id", user.getId())
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        applyBlogAuthors(records);
        return records.stream().map(this::toFeedDto).collect(Collectors.toList());
    }

    @Override
    public List<BlogFeedDto> pageBlogsForUser(Long userId, Integer current) {
        Page<Blog> page = query()
                .eq("user_id", userId)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        applyBlogAuthors(records);
        return records.stream().map(this::toFeedDto).collect(Collectors.toList());
    }

    @Override
    public void updateMyBlog(BlogUpdateCommand command) {
        if (command.getId() == null) {
            throw new BizException("\u7b14\u8bb0id\u4e0d\u80fd\u4e3a\u7a7a");
        }
        Long uid = UserHolder.getUser().getId();
        Blog existing = getById(command.getId());
        if (existing == null) {
            throw new BizException("\u7b14\u8bb0\u4e0d\u5b58\u5728\uff01");
        }
        if (!uid.equals(existing.getUserId())) {
            throw new BizException("\u65e0\u6743\u4fee\u6539\u8be5\u7b14\u8bb0");
        }
        LambdaUpdateWrapper<Blog> w = new LambdaUpdateWrapper<Blog>()
                .eq(Blog::getId, command.getId())
                .eq(Blog::getUserId, uid);
        if (command.getShopId() != null) {
            w.set(Blog::getShopId, command.getShopId());
        }
        if (StrUtil.isNotBlank(command.getTitle())) {
            w.set(Blog::getTitle, command.getTitle());
        }
        if (StrUtil.isNotBlank(command.getImages())) {
            w.set(Blog::getImages, command.getImages());
        }
        if (StrUtil.isNotBlank(command.getContent())) {
            w.set(Blog::getContent, command.getContent());
        }
        if (!update(w)) {
            throw new BizException("\u66f4\u65b0\u7b14\u8bb0\u5931\u8d25");
        }
    }

    @Override
    public void removeMyBlog(Long id) {
        Long uid = UserHolder.getUser().getId();
        Blog existing = getById(id);
        if (existing == null) {
            throw new BizException("\u7b14\u8bb0\u4e0d\u5b58\u5728\uff01");
        }
        if (!uid.equals(existing.getUserId())) {
            throw new BizException("\u65e0\u6743\u5220\u9664\u8be5\u7b14\u8bb0");
        }
        blogCommentsMapper.delete(new LambdaQueryWrapper<BlogComments>().eq(BlogComments::getBlogId, id));
        List<Follow> follows = followService.query().eq("follow_user_id", existing.getUserId()).list();
        for (Follow follow : follows) {
            String key = FEED_KEY + follow.getUserId();
            stringRedisTemplate.opsForZSet().remove(key, id.toString());
        }
        stringRedisTemplate.delete(BLOG_LIKED_KEY + id);
        if (!removeById(id)) {
            throw new BizException("\u5220\u9664\u7b14\u8bb0\u5931\u8d25");
        }
    }

    private void enrichBlog(Blog blog) {
        enrichBlogs(Collections.singletonList(blog));
    }

    /** ??????????????? N+1 ???? */
    private void applyBlogAuthors(List<Blog> blogs) {
        if (blogs == null || blogs.isEmpty()) {
            return;
        }
        Set<Long> userIds = blogs.stream().map(Blog::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        for (Blog blog : blogs) {
            User u = userMap.get(blog.getUserId());
            if (u != null) {
                blog.setName(u.getNickName());
                blog.setIcon(u.getIcon());
            }
        }
    }

    /**
     * ??????????????????? Redis ?????????? N+1?
     */
    private void enrichBlogs(List<Blog> blogs) {
        if (blogs == null || blogs.isEmpty()) {
            return;
        }
        applyBlogAuthors(blogs);
        CurrentUser current = UserHolder.getUser();
        if (current == null) {
            return;
        }
        String member = current.getId().toString();
        RedisSerializer<String> ser = stringRedisTemplate.getStringSerializer();
        List<Object> scores = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Blog blog : blogs) {
                byte[] keyBytes = ser.serialize(BLOG_LIKED_KEY + blog.getId());
                byte[] memberBytes = ser.serialize(member);
                connection.zSetCommands().zScore(keyBytes, memberBytes);
            }
            return null;
        });
        for (int i = 0; i < blogs.size(); i++) {
            Double score = (Double) scores.get(i);
            blogs.get(i).setIsLike(score != null);
        }
    }

    private BlogFeedDto toFeedDto(Blog blog) {
        return BeanUtil.copyProperties(blog, BlogFeedDto.class);
    }
}
