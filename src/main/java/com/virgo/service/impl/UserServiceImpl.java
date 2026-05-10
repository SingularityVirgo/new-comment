package com.virgo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virgo.common.exception.BizException;
import com.virgo.dto.LoginFormDTO;
import com.virgo.dto.UserDTO;
import com.virgo.entity.User;
import com.virgo.mapper.UserMapper;
import com.virgo.service.IUserService;
import com.virgo.utils.RegexUtils;
import com.virgo.utils.UserHolder;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Override
    public void sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            throw new BizException("手机号格式错误");
        }
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.debug("发送短信验证码成功，验证码：{}", code);
    }

    @Override
    public String login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            throw new BizException("手机号格式错误");
        }
        String catchCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (catchCode == null || !catchCode.equals(code)) {
            throw new BizException("验证码错误");
        }
        User user = query().eq("phone", phone).one();
        if (user == null) {
            user = createUserWithPhone(phone);
        }
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(
                userDTO,
                new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, stringObjectMap);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public void sign() {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyy:MM"));
        String key = USER_SIGN_KEY + userId + ":" + keySuffix;
        int dayOfMonth = now.getDayOfMonth();
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
    }

    @Override
    public Integer signCount() {
        Long userId = UserHolder.getUser().getId();
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
    public Optional<UserDTO> findUserDtoById(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(BeanUtil.copyProperties(user, UserDTO.class));
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_" + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
