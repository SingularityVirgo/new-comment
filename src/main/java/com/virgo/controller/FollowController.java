package com.virgo.controller;

import com.virgo.web.api.Result;
import com.virgo.service.IFollowService;
import com.virgo.web.assembly.WebModels;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    private final IFollowService followService;

    @PutMapping("/{id}/{isFollow}")
    public Result<?> follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow) {
        followService.follow(followUserId, isFollow);
        return Result.ok();
    }

    @GetMapping("/or/not/{id}")
    public Result<?> isFollow(@PathVariable("id") Long followUserId) {
        return Result.ok(followService.isFollow(followUserId));
    }

    @GetMapping("/common/{id}")
    public Result<?> followCommons(@PathVariable("id") Long id) {
        return Result.ok(WebModels.toMutualFollowUserVos(followService.followCommons(id)));
    }

    @GetMapping("/following/{id}")
    public Result<?> following(@PathVariable("id") Long id) {
        return Result.ok(WebModels.toFollowingUserVos(followService.listFollowing(id)));
    }
}
