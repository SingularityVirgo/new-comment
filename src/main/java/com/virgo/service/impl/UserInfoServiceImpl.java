package com.virgo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virgo.domain.dto.userinfo.UserInfoPublicDto;
import com.virgo.domain.dto.userinfo.UserInfoUpdateCommand;
import com.virgo.domain.po.UserInfo;
import com.virgo.mapper.UserInfoMapper;
import com.virgo.service.IUserInfoService;
import com.virgo.security.CurrentUserAccessor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    @Override
    public Optional<UserInfoPublicDto> findPresentableInfo(Long userId) {
        UserInfo info = getById(userId);
        if (info == null) {
            return Optional.empty();
        }
        UserInfoPublicDto dto = BeanUtil.copyProperties(info, UserInfoPublicDto.class);
        return Optional.of(dto);
    }

    @Override
    public void updateMyProfile(UserInfoUpdateCommand command) {
        Long uid = CurrentUserAccessor.require().getId();
        UserInfo row = getById(uid);
        if (row == null) {
            row = new UserInfo();
            row.setUserId(uid);
            row.setFans(0);
            row.setFollowee(0);
            row.setCredits(0);
            row.setLevel(false);
            row.setHideFollowing(false);
        }
        if (command.getCity() != null) {
            row.setCity(command.getCity());
        }
        if (command.getIntroduce() != null) {
            row.setIntroduce(command.getIntroduce());
        }
        if (command.getGender() != null) {
            row.setGender(command.getGender());
        }
        if (command.getBirthday() != null) {
            row.setBirthday(command.getBirthday());
        }
        if (command.getHideFollowing() != null) {
            row.setHideFollowing(command.getHideFollowing());
        }
        saveOrUpdate(row);
    }
}
