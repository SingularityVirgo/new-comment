package com.virgo.interceptor;

import com.virgo.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;


public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.判断是否需要拦截
        if(UserHolder.getUser()==null){
            response.setStatus(401);
            return false;
        }

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


}
