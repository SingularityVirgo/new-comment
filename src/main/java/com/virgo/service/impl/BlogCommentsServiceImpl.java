package com.virgo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virgo.common.exception.BizException;
import com.virgo.domain.dto.comment.BlogCommentItemDto;
import com.virgo.domain.dto.comment.CommentCreateCommand;
import com.virgo.domain.po.Blog;
import com.virgo.domain.po.BlogComments;
import com.virgo.domain.po.User;
import com.virgo.mapper.BlogCommentsMapper;
import com.virgo.service.IBlogCommentsService;
import com.virgo.service.IBlogService;
import com.virgo.service.IUserService;
import com.virgo.utils.SystemConstants;
import com.virgo.security.CurrentUserAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

    private final IBlogService blogService;
    private final IUserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(CommentCreateCommand command) {
        if (command.getBlogId() == null) {
            throw new BizException("\u7b14\u8bb0id\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (StrUtil.isBlank(command.getContent())) {
            throw new BizException("\u8bc4\u8bba\u5185\u5bb9\u4e0d\u80fd\u4e3a\u7a7a");
        }
        Blog blog = blogService.getById(command.getBlogId());
        if (blog == null) {
            throw new BizException("\u7b14\u8bb0\u4e0d\u5b58\u5728");
        }
        Long uid = CurrentUserAccessor.require().getId();
        BlogComments comment = new BlogComments();
        comment.setBlogId(command.getBlogId());
        comment.setContent(command.getContent().trim());
        comment.setUserId(uid);
        comment.setParentId(command.getParentId() != null ? command.getParentId() : 0L);
        comment.setAnswerId(command.getAnswerId() != null ? command.getAnswerId() : 0L);
        comment.setLiked(0);
        comment.setStatus(false);
        if (!save(comment)) {
            throw new BizException("\u53d1\u8868\u8bc4\u8bba\u5931\u8d25");
        }
        blogService.update().setSql("comments = IFNULL(comments,0) + 1").eq("id", comment.getBlogId()).update();
        return comment.getId();
    }

    @Override
    public List<BlogCommentItemDto> pageForBlog(Long blogId, Integer current) {
        if (blogService.getById(blogId) == null) {
            throw new BizException("\u7b14\u8bb0\u4e0d\u5b58\u5728");
        }
        LambdaQueryWrapper<BlogComments> q = new LambdaQueryWrapper<BlogComments>()
                .eq(BlogComments::getBlogId, blogId)
                .and(w -> w.isNull(BlogComments::getStatus).or().eq(BlogComments::getStatus, false))
                .orderByAsc(BlogComments::getId);
        Page<BlogComments> page = page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE), q);
        List<BlogComments> records = page.getRecords();
        return records.stream().map(c -> {
            BlogCommentItemDto dto = BeanUtil.copyProperties(c, BlogCommentItemDto.class);
            User u = userService.getById(c.getUserId());
            if (u != null) {
                dto.setName(u.getNickName());
                dto.setIcon(u.getIcon());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMyComment(Long id, String content) {
        if (StrUtil.isBlank(content)) {
            throw new BizException("\u8bc4\u8bba\u5185\u5bb9\u4e0d\u80fd\u4e3a\u7a7a");
        }
        BlogComments existing = getById(id);
        if (existing == null) {
            throw new BizException("\u8bc4\u8bba\u4e0d\u5b58\u5728");
        }
        if (!CurrentUserAccessor.require().getId().equals(existing.getUserId())) {
            throw new BizException("\u65e0\u6743\u4fee\u6539\u8be5\u8bc4\u8bba");
        }
        boolean ok = update(new LambdaUpdateWrapper<BlogComments>()
                .eq(BlogComments::getId, id)
                .set(BlogComments::getContent, content.trim()));
        if (!ok) {
            throw new BizException("\u66f4\u65b0\u8bc4\u8bba\u5931\u8d25");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMyComment(Long id) {
        BlogComments existing = getById(id);
        if (existing == null) {
            throw new BizException("\u8bc4\u8bba\u4e0d\u5b58\u5728");
        }
        if (!CurrentUserAccessor.require().getId().equals(existing.getUserId())) {
            throw new BizException("\u65e0\u6743\u5220\u9664\u8be5\u8bc4\u8bba");
        }
        if (!removeById(id)) {
            throw new BizException("\u5220\u9664\u8bc4\u8bba\u5931\u8d25");
        }
        blogService.update()
                .setSql("comments = GREATEST(IFNULL(comments,0) - 1, 0)")
                .eq("id", existing.getBlogId())
                .update();
    }
}
