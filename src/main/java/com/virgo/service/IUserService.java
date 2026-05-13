package com.virgo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virgo.domain.dto.auth.LoginRequest;
import com.virgo.domain.dto.user.NickNameUpdateCommand;
import com.virgo.domain.dto.user.PasswordChangeCommand;
import com.virgo.domain.dto.user.UserProfileDto;
import com.virgo.domain.po.User;
import com.virgo.domain.vo.user.MyAccountVo;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface IUserService extends IService<User> {

    void sendCode(String phone, HttpSession session);

    /** 向当前登录用户绑定手机号发送登录/改密用验证码 */
    void sendCodeToMyPhone(HttpSession session);

    String login(LoginRequest loginRequest, HttpSession session);

    /** 使 Redis 中的会话失效；SecurityContext 与 ThreadLocal 由过滤器在请求结束时清理。 */
    void logout(String authorizationHeaderToken);

    void sign();

    Integer signCount();

    Optional<UserProfileDto> findUserProfile(Long userId);

    MyAccountVo getMyAccount();

    void updateMyNickName(NickNameUpdateCommand command);

    void changeMyPassword(PasswordChangeCommand command, HttpSession session);

    /**
     * 上传并更新当前用户头像（OSS 或本地，由 {@link com.virgo.service.storage.UserAvatarStorage} 决定），返回新头像地址（完整 URL 或站内路径）。
     */
    String updateMyAvatar(MultipartFile file);
}
