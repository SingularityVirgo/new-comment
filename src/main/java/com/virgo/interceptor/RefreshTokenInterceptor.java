package com.virgo.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.virgo.dto.UserDTO;
import com.virgo.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.virgo.utils.RedisConstants.LOGIN_USER_KEY;
import static com.virgo.utils.RedisConstants.LOGIN_USER_TTL;

@Data
@AllArgsConstructor
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取请求头的token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            return true;
        }

        //2.基于token获取用户
        String key = LOGIN_USER_KEY + token;
        Map<Object, Object> objectMap = stringRedisTemplate.opsForHash().entries(key);
        //3.判断是否为空
        if (objectMap.isEmpty()) {
            return true;
        }
        //5.将查询到的hash值转为DTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(objectMap, new UserDTO(), false);

        //4.保存用户到ThreadLocal里
        UserHolder.saveUser(userDTO);
        //6.刷新token有效期
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);

        return true;
    }

//    private UserDTO convertToDTO(User user) {
//        UserDTO dto = new UserDTO();
//        // 复制需要的属性
//        dto.setId(user.getId());
//        dto.setNickName(user.getNickName());
//        dto.setIcon(user.getIcon());
//        // ... 其他需要的属性
//        return dto;
//    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户
        UserHolder.removeUser();
    }
}
