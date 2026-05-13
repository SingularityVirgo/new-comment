package com.virgo.domain.dto.userinfo;

import lombok.Data;

import java.time.LocalDate;

/** 当前用户更新个人资料 */
@Data
public class UserInfoUpdateCommand {
    private String city;
    private String introduce;
    private Boolean gender;
    private LocalDate birthday;
    /** null 表示不修改；true 对他人隐藏关注列表 */
    private Boolean hideFollowing;
}
