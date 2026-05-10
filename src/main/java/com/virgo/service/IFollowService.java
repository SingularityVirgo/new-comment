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
     * \u67e5\u8be2\u7528\u6237 {@code userId} \u5df2\u5173\u6ce8\u7684\u7528\u6237\u5217\u8868\uff0c\u8fd4\u56de\u524d\u7aef\u5c55\u793a\u7528 DTO\u3002
     */
    List<FollowingUserDto> listFollowing(Long userId);
}
