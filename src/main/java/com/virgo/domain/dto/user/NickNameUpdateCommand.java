package com.virgo.domain.dto.user;

import lombok.Data;

@Data
public class NickNameUpdateCommand {
    /** 对应表字段 nick_name（展示昵称） */
    private String nickName;
}
