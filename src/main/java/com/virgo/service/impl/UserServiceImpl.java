package com.virgo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virgo.common.exception.BizException;
import com.virgo.domain.dto.auth.CurrentUser;
import com.virgo.domain.dto.auth.LoginRequest;
import com.virgo.domain.dto.user.NickNameUpdateCommand;
import com.virgo.domain.dto.user.PasswordChangeCommand;
import com.virgo.domain.dto.user.UserProfileDto;
import com.virgo.domain.po.User;
import com.virgo.domain.vo.user.MyAccountVo;
import com.virgo.mapper.UserMapper;
import com.virgo.security.CurrentUserAccessor;
import com.virgo.service.IUserInfoService;
import com.virgo.service.IUserService;
import com.virgo.service.storage.UserAvatarStorage;
import com.virgo.utils.RegexUtils;
import com.virgo.web.assembly.WebModels;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.virgo.utils.RedisConstants.LOGIN_CODE_KEY;
import static com.virgo.utils.RedisConstants.LOGIN_CODE_TTL;
import static com.virgo.utils.RedisConstants.LOGIN_USER_KEY;
import static com.virgo.utils.RedisConstants.LOGIN_USER_TTL;
import static com.virgo.utils.RedisConstants.USER_SIGN_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final StringRedisTemplate stringRedisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final IUserInfoService userInfoService;
    private final UserAvatarStorage userAvatarStorage;

    private static final long MAX_AVATAR_BYTES = 5 * 1024 * 1024;
    public void sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            throw new BizException("\u624b\u673a\u53f7\u683c\u5f0f\u9519\u8bef");
        }
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.debug("\u53d1\u9001\u77ed\u4fe1\u9a8c\u8bc1\u7801\u6210\u529f\uff0c\u9a8c\u8bc1\u7801\uff1a{}", code);
    }

    @Override
    public String login(LoginRequest loginRequest, HttpSession session) {
        String phone = loginRequest.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            throw new BizException("\u624b\u673a\u53f7\u683c\u5f0f\u9519\u8bef");
        }
        if (StrUtil.isNotBlank(loginRequest.getPassword())) {
            return loginByPassword(phone, loginRequest.getPassword());
        }
        return loginByCode(phone, loginRequest.getCode());
    }

    private String loginByCode(String phone, String code) {
        String catchCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if (catchCode == null || !catchCode.equals(code)) {
            throw new BizException("\u9a8c\u8bc1\u7801\u9519\u8bef");
        }
        User user = query().eq("phone", phone).one();
        if (user == null) {
            user = createUserWithPhone(phone);
        }
        return issueToken(user);
    }

    private String loginByPassword(String phone, String rawPassword) {
        User user = query().eq("phone", phone).one();
        if (user == null) {
            throw new BizException("\u7528\u6237\u4e0d\u5b58\u5728\uff0c\u8bf7\u5148\u4f7f\u7528\u624b\u673a\u53f7\u9a8c\u8bc1\u7801\u767b\u5f55\u6ce8\u518c");
        }
        if (StrUtil.isBlank(user.getPassword())) {
            throw new BizException("\u672a\u8bbe\u7f6e\u5bc6\u7801\u8bf7\u7528\u624b\u673a\u53f7\u767b\u5f55");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BizException("\u5bc6\u7801\u9519\u8bef");
        }
        return issueToken(user);
    }

    private String issueToken(User user) {
        String token = UUID.randomUUID().toString(true);
        CurrentUser currentUser = new CurrentUser();
        currentUser.setId(user.getId());
        currentUser.setPhone(user.getPhone());
        currentUser.setNickName(user.getNickName());
        currentUser.setIcon(user.getIcon() == null ? "" : user.getIcon());
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(
                currentUser,
                new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, stringObjectMap);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public void sendCodeToMyPhone(HttpSession session) {
        Long id = CurrentUserAccessor.require().getId();
        User user = getById(id);
        if (user == null || StrUtil.isBlank(user.getPhone())) {
            throw new BizException("\u672a\u7ed1\u5b9a\u624b\u673a\u53f7");
        }
        sendCode(user.getPhone(), session);
    }

    @Override
    public MyAccountVo getMyAccount() {
        Long id = CurrentUserAccessor.require().getId();
        User user = getById(id);
        if (user == null) {
            throw new BizException("\u7528\u6237\u4e0d\u5b58\u5728");
        }
        MyAccountVo vo = new MyAccountVo();
        vo.setId(user.getId());
        vo.setNickName(user.getNickName());
        vo.setIcon(user.getIcon() == null ? "" : user.getIcon());
        vo.setPhoneMasked(maskPhone(user.getPhone()));
        vo.setHasPassword(StrUtil.isNotBlank(user.getPassword()));
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        userInfoService.findPresentableInfo(user.getId()).ifPresent(dto -> vo.setUserInfo(WebModels.toUserInfoVo(dto)));
        return vo;
    }

    @Override
    public void updateMyNickName(NickNameUpdateCommand command) {
        String nick = command.getNickName() == null ? "" : command.getNickName().trim();
        if (StrUtil.isBlank(nick)) {
            throw new BizException("\u6635\u79f0\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (nick.length() > 32) {
            throw new BizException("\u6635\u79f0\u4e0d\u80fd\u8d85\u8fc732\u4e2a\u5b57\u7b26");
        }
        Long id = CurrentUserAccessor.require().getId();
        lambdaUpdate().eq(User::getId, id).set(User::getNickName, nick).update();
        syncPrincipalNickName(nick);
    }

    @Override
    public void changeMyPassword(PasswordChangeCommand command, HttpSession session) {
        Long id = CurrentUserAccessor.require().getId();
        User user = getById(id);
        if (user == null || StrUtil.isBlank(user.getPhone())) {
            throw new BizException("\u672a\u7ed1\u5b9a\u624b\u673a\u53f7");
        }
        String newPassword = command.getNewPassword();
        if (StrUtil.isBlank(newPassword) || newPassword.length() < 6) {
            throw new BizException("\u5bc6\u7801\u81f3\u5c116\u4f4d");
        }
        if (newPassword.length() > 64) {
            throw new BizException("\u5bc6\u7801\u8fc7\u957f");
        }
        String catchCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + user.getPhone());
        if (catchCode == null || !catchCode.equals(command.getCode())) {
            throw new BizException("\u9a8c\u8bc1\u7801\u9519\u8bef");
        }
        stringRedisTemplate.delete(LOGIN_CODE_KEY + user.getPhone());
        String encoded = passwordEncoder.encode(newPassword);
        lambdaUpdate().eq(User::getId, id).set(User::getPassword, encoded).update();
    }

    @Override
    public String updateMyAvatar(MultipartFile file) {
        validateAvatarFile(file);
        Long id = CurrentUserAccessor.require().getId();
        User user = getById(id);
        if (user == null) {
            throw new BizException("\u7528\u6237\u4e0d\u5b58\u5728");
        }
        String previous = user.getIcon();
        String stored;
        try {
            stored = userAvatarStorage.store(file);
        } catch (java.io.IOException e) {
            throw new BizException("\u5934\u50cf\u4e0a\u4f20\u5931\u8d25", e);
        }
        if (stored.length() > 255) {
            throw new BizException("\u5934\u50cf\u5730\u5740\u8fc7\u957f");
        }
        tryDeletePreviousAvatarIfOwned(previous);
        lambdaUpdate().eq(User::getId, id).set(User::getIcon, stored).update();
        syncPrincipalIcon(stored);
        return stored;
    }

    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("\u8bf7\u9009\u62e9\u56fe\u7247\u6587\u4ef6");
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new BizException("\u56fe\u7247\u4e0d\u80fd\u8d85\u8fc7 5MB");
        }
        String ct = file.getContentType();
        boolean ok = StrUtil.isNotBlank(ct)
                && (ct.contains("image/jpeg") || ct.contains("image/png") || ct.contains("image/webp") || ct.contains("image/gif"));
        if (!ok) {
            String name = file.getOriginalFilename();
            if (StrUtil.isNotBlank(name)) {
                String suf = StrUtil.subAfter(name, ".", true).toLowerCase();
                ok = Set.of("jpg", "jpeg", "png", "webp", "gif").contains(suf);
            }
        }
        if (!ok) {
            throw new BizException("\u4ec5\u652f\u6301 jpg\u3001png\u3001webp\u3001gif \u56fe\u7247");
        }
    }

    private void tryDeletePreviousAvatarIfOwned(String previous) {
        if (!StrUtil.isNotBlank(previous) || !previous.contains("/avatars/")) {
            return;
        }
        try {
            userAvatarStorage.delete(previous);
        } catch (Exception ex) {
            log.warn("delete previous avatar skipped: {}", ex.getMessage());
        }
    }

    private void syncPrincipalIcon(String icon) {
        CurrentUser cu = CurrentUserAccessor.get();
        if (cu == null) {
            return;
        }
        String v = icon == null ? "" : icon;
        cu.setIcon(v);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof UsernamePasswordAuthenticationToken token)) {
            return;
        }
        UsernamePasswordAuthenticationToken replacement =
                new UsernamePasswordAuthenticationToken(cu, token.getCredentials(), token.getAuthorities());
        replacement.setDetails(token.getDetails());
        SecurityContextHolder.getContext().setAuthentication(replacement);
        Object cred = token.getCredentials();
        if (cred instanceof String tk && StrUtil.isNotBlank(tk)) {
            stringRedisTemplate.opsForHash().put(LOGIN_USER_KEY + tk, "icon", v);
        }
    }

    private void syncPrincipalNickName(String nickName) {
        CurrentUser cu = CurrentUserAccessor.get();
        if (cu == null) {
            return;
        }
        cu.setNickName(nickName);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof UsernamePasswordAuthenticationToken token)) {
            return;
        }
        UsernamePasswordAuthenticationToken replacement =
                new UsernamePasswordAuthenticationToken(cu, token.getCredentials(), token.getAuthorities());
        replacement.setDetails(token.getDetails());
        SecurityContextHolder.getContext().setAuthentication(replacement);
        Object cred = token.getCredentials();
        if (cred instanceof String tk && StrUtil.isNotBlank(tk)) {
            stringRedisTemplate.opsForHash().put(LOGIN_USER_KEY + tk, "nickName", nickName);
        }
    }

    private static String maskPhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return "";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    @Override
    public void logout(String authorizationHeaderToken) {
        if (StrUtil.isNotBlank(authorizationHeaderToken)) {
            stringRedisTemplate.delete(LOGIN_USER_KEY + authorizationHeaderToken);
        }
    }

    @Override
    public void sign() {
        Long userId = CurrentUserAccessor.require().getId();
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyy:MM"));
        String key = USER_SIGN_KEY + userId + ":" + keySuffix;
        int dayOfMonth = now.getDayOfMonth();
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
    }

    @Override
    public Integer signCount() {
        Long userId = CurrentUserAccessor.require().getId();
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyy:MM"));
        String key = USER_SIGN_KEY + userId + ":" + keySuffix;
        int dayOfMonth = now.getDayOfMonth();
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));
        if (result == null || result.isEmpty()) {
            return 0;
        }
        Long num = result.get(0);
        if (num == null || num == 0L) {
            return 0;
        }
        int count = 0;
        while (true) {
            if ((num & 1) == 0) {
                break;
            }
            count++;
            num >>>= 1;
        }
        return count;
    }

    @Override
    public Optional<UserProfileDto> findUserProfile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(BeanUtil.copyProperties(user, UserProfileDto.class));
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_" + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
