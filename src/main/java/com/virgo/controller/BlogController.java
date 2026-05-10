package com.virgo.controller;

import com.virgo.dto.Result;
import com.virgo.entity.Blog;
import com.virgo.service.IBlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogController {

    private final IBlogService blogService;

    @PostMapping
    public Result<?> saveBlog(@RequestBody Blog blog) {
        return Result.ok(blogService.saveBlog(blog));
    }

    @PutMapping("/like/{id}")
    public Result<?> likeBlog(@PathVariable("id") Long id) {
        blogService.likeBlog(id);
        return Result.ok();
    }

    @GetMapping("/of/me")
    public Result<?> queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return Result.ok(blogService.pageBlogsForCurrentUser(current));
    }

    @GetMapping("/hot")
    public Result<?> queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return Result.ok(blogService.queryHotBlog(current));
    }

    @GetMapping("/{id}")
    public Result<?> queryBlogById(@PathVariable("id") Long id) {
        return Result.ok(blogService.queryBlogById(id));
    }

    @GetMapping("/likes/{id}")
    public Result<?> queryBlogLikes(@PathVariable("id") Long id) {
        return Result.ok(blogService.queryBlogLikes(id));
    }

    @GetMapping("/of/user")
    public Result<?> queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        return Result.ok(blogService.pageBlogsForUser(id, current));
    }

    @GetMapping("/of/follow")
    public Result<?> queryBlogOfFollow(
            @RequestParam("lastId") Long max,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return Result.ok(blogService.queryBlogOfFollow(max, offset));
    }
}
