package com.virgo.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author Ë??Â?•
 * @since 2021-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_shop")
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ‰∏ªÈ?Æ
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Â??È?∫ÂêçÁß∞
     */
    private String name;

    /**
     * Â??È?∫Á±ªÂ??Á??id
     */
    private Long typeId;

    /** ???? id?C ?????????? */
    private Long merchantId;

    /**
     * Â??È?∫Â?æÁ??Ôº?Â§?‰∏™Â?æÁ??‰ª•','È??Âº?
     */
    private String images;

    /**
     * Â??Â??Ôº?‰æ?Â¶?È??ÂÆ∂Â?¥
     */
    private String area;

    /**
     * Â?∞Âù?
     */
    private String address;

    /**
     * ÁªèÂ∫¶
     */
    private Double x;

    /**
     * Áª¥Â∫¶
     */
    private Double y;

    /**
     * Âù?‰ª∑Ôº?Âè?Ê?¥Ê?∞
     */
    private Long avgPrice;

    /**
     * È??È??
     */
    private Integer sold;

    /**
     * ËØ?ËÆ∫Ê?∞È?è
     */
    private Integer comments;

    /**
     * ËØ?Â??Ôº?~5Â??Ôº?‰π?0‰øùÂ≠?Ôº?ÈÅøÂ?çÂ∞èÊ??
     */
    private Integer score;

    /**
     * Ëê•‰∏?Ê?∂È?¥Ôº?‰æ?Â¶?10:00-22:00
     */
    private String openHours;

    /**
     * Â??Âª∫Ê?∂È?¥
     */
    private LocalDateTime createTime;

    /**
     * Ê?¥Ê?∞Ê?∂È?¥
     */
    private LocalDateTime updateTime;


    @TableField(exist = false)
    private Double distance;
}
