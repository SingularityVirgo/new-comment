export interface UserInfoDTO {
  userId: number;
  city?: string;
  introduce?: string;
  fans?: number;
  followee?: number;
  gender?: boolean;
  birthday?: string;
  credits?: number;
  level?: boolean;
}

export interface UserDTO {
  id: number;
  nickName: string;
  icon: string;
  /** 当前登录用户是否已关注该用户（由部分接口填充） */
  isFollow?: boolean;
  /** 以下字段由 GET /user/me 返回 */
  phoneMasked?: string;
  hasPassword?: boolean;
  createTime?: string;
  updateTime?: string;
  userInfo?: UserInfoDTO | null;
}

export interface BlogComment {
  id: number;
  userId: number;
  blogId: number;
  parentId: number;
  answerId: number;
  content: string;
  liked?: number;
  status?: boolean;
  createTime?: string;
  updateTime?: string;
  name?: string;
  icon?: string;
}

export interface Blog {
  id: number;
  shopId: number;
  userId: number;
  title: string;
  images: string;
  content: string;
  liked: number;
  comments: number;
  createTime?: string;
  updateTime?: string;
  name?: string;
  icon?: string;
  isLike?: boolean;
}

export interface ShopType {
  id: number;
  name: string;
  icon: string;
  sort?: number;
}

export interface Shop {
  id: number;
  name: string;
  typeId: number;
  images: string;
  area?: string;
  address: string;
  x: number;
  y: number;
  avgPrice?: number;
  sold: number;
  comments: number;
  score: number;
  openHours?: string;
  createTime?: string;
  updateTime?: string;
  distance?: number;
}

export interface Voucher {
  id: number;
  shopId: number;
  title: string;
  subTitle?: string;
  rules?: string;
  payValue: number;
  actualValue: number;
  type: number;
  status: number;
  stock?: number;
  beginTime?: string;
  endTime?: string;
  createTime?: string;
  updateTime?: string;
}

export interface ScrollResult {
  list: Blog[];
  minTime: number;
  offset: number;
}
