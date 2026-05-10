package com.virgo.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virgo.common.exception.BizException;
import com.virgo.entity.Blog;
import com.virgo.entity.BlogComments;
import com.virgo.entity.User;
import com.virgo.mapper.BlogCommentsMapper;
import com.virgo.service.IBlogCommentsService;
import com.virgo.service.IBlogService;
import com.virgo.service.IUserService;
import com.virgo.utils.SystemConstants;
import com.virgo.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

    private final IBlogService blogService;
    private final IUserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(BlogComments comment) {
        if (comment.getBlogId() == null) {
            throw new BizException("笔记id不能为空");
        }
        if (StrUtil.isBlank(comment.getContent())) {
            throw new BizException("评论内容不能为空");
        }
        Blog blog = blogService.getById(comment.getBlogId());
        if (blog == null) {
            throw new BizException("笔记不存在");
        }
        Long uid = UserHolder.getUser().getId();
        comment.setUserId(uid);
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        if (comment.getAnswerId() == null) {
            comment.setAnswerId(0L);
        }
        if (comment.getLiked() == null) {
            comment.setLiked(0);
        }
        if (comment.getStatus() == null) {
            comment.setStatus(false);
        }
        if (!save(comment)) {
            throw new BizException("发表评论失败");
        }
        blogService.update().setSql("comments = IFNULL(comments,0) + 1").eq("id", comment.getBlogId()).update();
        return comment.getId();
    }

    @Override
    public List<BlogComments> pageForBlog(Long blogId, Integer current) {
        if (blogService.getById(blogId) == null) {
            throw new BizException("笔记不存在");
        }
        LambdaQueryWrapper<BlogComments> q = new LambdaQueryWrapper<BlogComments>()
                .eq(BlogComments::getBlogId, blogId)
                .and(w -> w.isNull(BlogComments::getStatus).or().eq(BlogComments::getStatus, false))
                .orderByAsc(BlogComments::getId);
        Page<BlogComments> page = page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE), q);
        List<BlogComments> records = page.getRecords();
        for (BlogComments c : records) {
            User u = userService.getById(c.getUserId());
            if (u != null) {
                c.setName(u.getNickName());
                c.setIcon(u.getIcon());
            }
        }
        return records;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMyComment(Long id, String content) {
        if (StrUtil.isBlank(content)) {
            throw new BizException("评论内容不能为空");
        }
        BlogComments existing = getById(id);
        if (existing == null) {
            throw new BizException("评论不存在");
        }
        if (!UserHolder.getUser().getId().equals(existing.getUserId())) {
            throw new BizException("无权修改该评论");
        }
        boolean ok = update(new LambdaUpdateWrapper<BlogComments>()
                .eq(BlogComments::getId, id)
                .set(BlogComments::getContent, content.trim()));
        if (!ok) {
            throw new BizException("更新评论失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMyComment(Long id) {
        BlogComments existing = getById(id);
        if (existing == null) {
            throw new BizException("评论不存在");
        }
        if (!UserHolder.getUser().getId().equals(existing.getUserId())) {
            throw new BizException("无权删除该评论");
        }
        if (!removeById(id)) {
            throw new BizException("删除评论失败");
        }
        blogService.update()
                .setSql("comments = GREATEST(IFNULL(comments,0) - 1, 0)")
                .eq("id", existing.getBlogId())
                .update();
    }
}
