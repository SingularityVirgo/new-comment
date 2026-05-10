export interface UserDTO {
  id: number;
  nickName: string;
  icon: string;
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
