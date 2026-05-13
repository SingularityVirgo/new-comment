package com.virgo.domain.vo.userinfo;

import lombok.Data;

import java.time.LocalDate;

/** 用户扩展资料（接口响应） */
@Data
public class UserInfoVo {
    private Long userId;
    private String city;
    private String introduce;
    private Integer fans;
    private Integer followee;
    private Boolean gender;
    private LocalDate birthday;
    private Integer credits;
    private Boolean level;
    private Boolean hideFollowing;
}
