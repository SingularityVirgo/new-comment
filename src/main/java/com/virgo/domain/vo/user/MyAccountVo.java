package com.virgo.domain.vo.user;

import com.virgo.domain.vo.userinfo.UserInfoVo;
import lombok.Data;

import java.time.LocalDateTime;

/** 当前登录用户账户信息（含脱敏手机号、是否已设密码、扩展资料） */
@Data
public class MyAccountVo {
    private Long id;
    private String nickName;
    private String icon;
    /** 脱敏手机号，如 138****8000 */
    private String phoneMasked;
    private boolean hasPassword;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 来自 tb_user_info，可能为空 */
    private UserInfoVo userInfo;
}
