package com.virgo.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author Ë??Â?•
 * @since 2021-12-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_user_info")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ‰∏ªÈ?ÆÔº?Á?®Ê?∑id
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * Â??Â∏?ÂêçÁß∞
     */
    private String city;

    /**
     * ‰∏™‰∫∫‰ª?ÁªçÔº?‰∏çË¶ÅË∂?Ëø?28‰∏™Â≠?Á¨?
     */
    private String introduce;

    /**
     * Á≤?‰∏ùÊ?∞È?è
     */
    private Integer fans;

    /**
     * Â?≥Ê≥®Á??‰∫∫Á??Ê?∞È??
     */
    private Integer followee;

    /**
     * Ê?ßÂ?´Ôº?Ôº?Á?∑Ôº?Ôº?Â•≥
     */
    private Boolean gender;

    /**
     * Á??Ê?•
     */
    private LocalDate birthday;

    /**
     * ÁßØÂ??
     */
    private Integer credits;

    /**
     * ‰º?Â??Á∫ßÂ?´Ôº?~9Á∫?0‰ª£Ë°®Ê?™Âº?È??‰º?Â??
     */
    private Boolean level;

    /** ??????????? */
    private Boolean hideFollowing;

    /**
     * Â??Âª∫Ê?∂È?¥
     */
    private LocalDateTime createTime;

    /**
     * Ê?¥Ê?∞Ê?∂È?¥
     */
    private LocalDateTime updateTime;


}
