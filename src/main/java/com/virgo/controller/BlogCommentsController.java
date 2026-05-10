package com.virgo.controller;

import com.virgo.dto.Result;
import com.virgo.entity.BlogComments;
import com.virgo.service.IBlogCommentsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blog-comments")
@RequiredArgsConstructor
public class BlogCommentsController {

    private final IBlogCommentsService blogCommentsService;

    @PostMapping
    public Result<?> add(@RequestBody BlogComments comment) {
        return Result.ok(blogCommentsService.addComment(comment));
    }

    @GetMapping("/of-blog/{blogId}")
    public Result<?> pageByBlog(
            @PathVariable("blogId") Long blogId,
            @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return Result.ok(blogCommentsService.pageForBlog(blogId, current));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable("id") Long id, @RequestBody CommentContentBody body) {
        blogCommentsService.updateMyComment(id, body.getContent());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") Long id) {
        blogCommentsService.removeMyComment(id);
        return Result.ok();
    }

    @Data
    public static class CommentContentBody {
        private String content;
    }
}
