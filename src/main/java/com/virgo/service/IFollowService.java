package com.virgo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virgo.domain.dto.follow.FollowingUserDto;
import com.virgo.domain.dto.follow.MutualFollowUserDto;
import com.virgo.domain.po.Follow;

import java.util.List;

public interface IFollowService extends IService<Follow> {

    void follow(Long followUserId, Boolean isFollow);

    Boolean isFollow(Long followUserId);

    List<MutualFollowUserDto> followCommons(Long id);

    /**
     * ?? {@code userId} ????????????????
     * ????????? {@code userId}????? {@code isFollow} ?? true?
     * ??????????????????
     */
    List<FollowingUserDto> listFollowing(Long userId);
}
