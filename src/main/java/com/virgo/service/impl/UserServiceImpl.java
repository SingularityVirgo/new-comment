package com.virgo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virgo.dto.LoginFormDTO;
import com.virgo.dto.Result;
import com.virgo.dto.UserDTO;
import com.virgo.entity.User;
import com.virgo.mapper.UserMapper;
import com.virgo.service.IUserService;
import com.virgo.utils.RegexUtils;
import com.virgo.utils.UserHolder;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.virgo.utils.RedisConstants.*;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        } else {
            //生成验证码
            String code = RandomUtil.randomNumbers(6);
            //保存验证码
//            session.setAttribute("code", code);
            stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
            //发送验证码
            log.debug("发送短信验证码成功，验证码：{}", code);

            //返回结果
            return Result.ok();
        }

    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1. 校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        //2. 校验验证码
//        Object CatchCode = session.getAttribute("code");
        String catchCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (catchCode == null || !catchCode.equals(code)) {
            //3. 不一致报错
            return Result.fail("验证码错误");
        }
        //4. 一致，查询用户
        User user = query().eq("phone", phone).one();
        //5. 不存在，创建新用户并保存
        if (user == null) {
            user = createUserWithPhone(phone);
        }
//        //6. 保存用户到session并返回结果
//        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        //6.1随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);
        //6.2将user转为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        //6.3保存
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, stringObjectMap);
        //6.4设置token有效期 用拦截器刷新token有效期
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);

        return Result.ok(token);
    }

    @Override
    public Result sign() {
        //1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        //2.获取日期
        LocalDateTime now = LocalDateTime.now();
        //3.拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyy:MM"));
        String key = USER_SIGN_KEY + userId + ":" + keySuffix;
        //4.获取今天是第几天
        int dayOfMonth = now.getDayOfMonth();
        //5.写入redis set bit key offset 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
        //1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        //2.获取日期
        LocalDateTime now = LocalDateTime.now();
        //3.拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyy:MM"));
        String key = USER_SIGN_KEY + userId + ":" + keySuffix;
        //4.获取今天是第几天
        int dayOfMonth = now.getDayOfMonth();
        //5.获取本月到今日为止的所有签到记录
        List<Long> result = stringRedisTemplate.opsForValue().bitField(key, BitFieldSubCommands.create().get
                (BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));
        if(result == null || result.isEmpty()){
            return Result.ok(0);
        }
        Long num = result.get(0);
        if(num == null|| num == 0L){
            return Result.ok(0);
        }
        //6.循环遍历
        int count = 0;
        while (true){
            //7.让这个数字与1做与运算，得到数字最后一个bit位
            // 如果为0，说明未签到，结束
            if((num & 1) == 0){
                break;
            }else {
                //9.如果为1，已签到，计数器+1
                count++;
            }
            //10.把数字右移一位，抛弃最后一个bit位，继续判断下一个bit位
            num >>>=1;

        }
        return Result.ok(count);











    }

    private User createUserWithPhone(String phone) {
        User user;
        user = new User();
        user.setPhone(phone);
        user.setNickName("user_" + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
