package com.virgo.domain.vo.merchant;

import lombok.Data;

/** C 端可见的商户信息 */
@Data
public class MerchantPublicVo {
    private Long id;
    private String name;
    private String intro;
    private String phone;
}
