package com.virgo.domain.dto.userinfo;

import lombok.Data;

import java.time.LocalDate;

/** 用户扩展资料（服务层，可对外展示字段） */
@Data
public class UserInfoPublicDto {
    private Long userId;
    private String city;
    private String introduce;
    private Integer fans;
    private Integer followee;
    private Boolean gender;
    private LocalDate birthday;
    private Integer credits;
    private Boolean level;
}
