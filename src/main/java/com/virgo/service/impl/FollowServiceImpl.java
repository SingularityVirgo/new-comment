package com.virgo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virgo.domain.dto.follow.FollowingUserDto;
import com.virgo.domain.dto.follow.MutualFollowUserDto;
import com.virgo.domain.po.Follow;
import com.virgo.domain.po.User;
import com.virgo.domain.po.UserInfo;
import com.virgo.mapper.FollowMapper;
import com.virgo.service.IFollowService;
import com.virgo.service.IUserInfoService;
import com.virgo.service.IUserService;
import com.virgo.security.CurrentUserAccessor;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;
    @Resource
    private IUserInfoService userInfoService;

    @Override
    public void follow(Long followUserId, Boolean isFollow) {
        Long userId = CurrentUserAccessor.require().getId();
        String key = "follows:" + userId;
        if (isFollow) {
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if (isSuccess) {
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId).eq("follow_user_id", followUserId));
            if (isSuccess) {
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
    }

    @Override
    public Boolean isFollow(Long followUserId) {
        Long userId = CurrentUserAccessor.require().getId();
        Integer count = Math.toIntExact(query().eq("user_id", userId).eq("follow_user_id", followUserId).count());
        return count > 0;
    }

    @Override
    public List<MutualFollowUserDto> followCommons(Long id) {
        Long userId = CurrentUserAccessor.require().getId();
        String key = "follows:" + userId;
        String key2 = "follows:" + id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, key2);
        if (intersect == null || intersect.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        return userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, MutualFollowUserDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<FollowingUserDto> listFollowing(Long userId) {
        Long viewerId = CurrentUserAccessor.require().getId();
        if (!viewerId.equals(userId)) {
            UserInfo targetInfo = userInfoService.getById(userId);
            if (targetInfo != null && Boolean.TRUE.equals(targetInfo.getHideFollowing())) {
                return Collections.emptyList();
            }
        }
        List<Follow> follows = query().eq("user_id", userId).orderByDesc("create_time").list();
        if (follows.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = follows.stream().map(Follow::getFollowUserId).collect(Collectors.toList());
        Map<Long, User> userMap = userService.listByIds(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        List<FollowingUserDto> result = ids.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(user -> BeanUtil.copyProperties(user, FollowingUserDto.class))
                .collect(Collectors.toList());

        if (viewerId.equals(userId)) {
            for (FollowingUserDto u : result) {
                u.setIsFollow(true);
            }
            return result;
        }
        if (result.isEmpty()) {
            return result;
        }
        List<Long> targetIds = result.stream().map(FollowingUserDto::getId).collect(Collectors.toList());
        Set<Long> followedIds = query().eq("user_id", viewerId).in("follow_user_id", targetIds).list()
                .stream()
                .map(Follow::getFollowUserId)
                .collect(Collectors.toSet());
        for (FollowingUserDto u : result) {
            u.setIsFollow(followedIds.contains(u.getId()));
        }
        return result;
    }
}
