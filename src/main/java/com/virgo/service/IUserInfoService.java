package com.virgo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virgo.domain.dto.userinfo.UserInfoPublicDto;
import com.virgo.domain.dto.userinfo.UserInfoUpdateCommand;
import com.virgo.domain.po.UserInfo;

import java.util.Optional;

public interface IUserInfoService extends IService<UserInfo> {

    Optional<UserInfoPublicDto> findPresentableInfo(Long userId);

    void updateMyProfile(UserInfoUpdateCommand command);
}
