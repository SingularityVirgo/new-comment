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
        UserInfo patch = new UserInfo();
        patch.setUserId(uid);
        patch.setCity(command.getCity());
        patch.setIntroduce(command.getIntroduce());
        patch.setGender(command.getGender());
        patch.setBirthday(command.getBirthday());
        saveOrUpdate(patch);
    }
}
