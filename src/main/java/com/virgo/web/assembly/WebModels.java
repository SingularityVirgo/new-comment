package com.virgo.web.assembly;

import cn.hutool.core.bean.BeanUtil;
import com.virgo.domain.dto.auth.CurrentUser;
import com.virgo.domain.dto.comment.BlogCommentItemDto;
import com.virgo.domain.dto.blog.BlogFeedDto;
import com.virgo.domain.dto.blog.BlogFollowScrollDto;
import com.virgo.domain.dto.blog.BlogLikeUserDto;
import com.virgo.domain.dto.follow.FollowingUserDto;
import com.virgo.domain.dto.follow.MutualFollowUserDto;
import com.virgo.domain.dto.user.UserProfileDto;
import com.virgo.domain.dto.userinfo.UserInfoPublicDto;
import com.virgo.domain.po.Shop;
import com.virgo.domain.po.ShopType;
import com.virgo.domain.po.Voucher;
import com.virgo.domain.vo.comment.BlogCommentVo;
import com.virgo.domain.vo.blog.BlogFeedVo;
import com.virgo.domain.vo.blog.BlogFollowScrollVo;
import com.virgo.domain.vo.blog.BlogLikeUserVo;
import com.virgo.domain.vo.follow.FollowingUserVo;
import com.virgo.domain.vo.follow.MutualFollowUserVo;
import com.virgo.domain.vo.shop.ShopDetailVo;
import com.virgo.domain.vo.shop.ShopListItemVo;
import com.virgo.domain.vo.shoptype.ShopTypeVo;
import com.virgo.domain.vo.user.UserProfileVo;
import com.virgo.domain.vo.userinfo.UserInfoVo;
import com.virgo.domain.vo.voucher.VoucherVo;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO / PO 与 VO 之间的装配（保持各层类型分离）。
 */
public final class WebModels {

    private WebModels() {
    }

    public static UserProfileVo toUserProfileVo(UserProfileDto dto) {
        return copy(dto, UserProfileVo.class);
    }

    public static UserProfileVo toUserProfileVo(CurrentUser u) {
        return copy(u, UserProfileVo.class);
    }

    public static UserInfoVo toUserInfoVo(UserInfoPublicDto dto) {
        return copy(dto, UserInfoVo.class);
    }

    public static FollowingUserVo toFollowingUserVo(FollowingUserDto dto) {
        return copy(dto, FollowingUserVo.class);
    }

    public static List<FollowingUserVo> toFollowingUserVos(List<FollowingUserDto> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(WebModels::toFollowingUserVo).collect(Collectors.toList());
    }

    public static MutualFollowUserVo toMutualFollowUserVo(MutualFollowUserDto dto) {
        return copy(dto, MutualFollowUserVo.class);
    }

    public static List<MutualFollowUserVo> toMutualFollowUserVos(List<MutualFollowUserDto> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(WebModels::toMutualFollowUserVo).collect(Collectors.toList());
    }

    public static BlogFeedVo toBlogFeedVo(BlogFeedDto dto) {
        return copy(dto, BlogFeedVo.class);
    }

    public static List<BlogFeedVo> toBlogFeedVos(List<BlogFeedDto> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(WebModels::toBlogFeedVo).collect(Collectors.toList());
    }

    public static BlogFollowScrollVo toBlogFollowScrollVo(BlogFollowScrollDto dto) {
        if (dto == null) {
            return null;
        }
        BlogFollowScrollVo vo = new BlogFollowScrollVo();
        vo.setList(dto.getList() == null ? Collections.emptyList() : toBlogFeedVos(dto.getList()));
        vo.setMinTime(dto.getMinTime());
        vo.setOffset(dto.getOffset());
        return vo;
    }

    public static BlogLikeUserVo toBlogLikeUserVo(BlogLikeUserDto dto) {
        return copy(dto, BlogLikeUserVo.class);
    }

    public static List<BlogLikeUserVo> toBlogLikeUserVos(List<BlogLikeUserDto> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(WebModels::toBlogLikeUserVo).collect(Collectors.toList());
    }

    public static BlogCommentVo toBlogCommentVo(BlogCommentItemDto dto) {
        return copy(dto, BlogCommentVo.class);
    }

    public static List<BlogCommentVo> toBlogCommentVos(List<BlogCommentItemDto> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(WebModels::toBlogCommentVo).collect(Collectors.toList());
    }

    public static ShopDetailVo toShopDetailVo(Shop shop) {
        return copy(shop, ShopDetailVo.class);
    }

    public static ShopListItemVo toShopListItemVo(Shop shop) {
        return copy(shop, ShopListItemVo.class);
    }

    public static List<ShopListItemVo> toShopListItemVos(List<Shop> shops) {
        if (shops == null) {
            return Collections.emptyList();
        }
        return shops.stream().map(WebModels::toShopListItemVo).collect(Collectors.toList());
    }

    public static ShopTypeVo toShopTypeVo(ShopType type) {
        return copy(type, ShopTypeVo.class);
    }

    public static List<ShopTypeVo> toShopTypeVos(List<ShopType> types) {
        if (types == null) {
            return Collections.emptyList();
        }
        return types.stream().map(WebModels::toShopTypeVo).collect(Collectors.toList());
    }

    public static VoucherVo toVoucherVo(Voucher v) {
        return copy(v, VoucherVo.class);
    }

    public static List<VoucherVo> toVoucherVos(List<Voucher> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(WebModels::toVoucherVo).collect(Collectors.toList());
    }

    private static <S, T> T copy(S source, Class<T> targetClass) {
        return BeanUtil.copyProperties(source, targetClass);
    }
}
