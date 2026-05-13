/*
  为已有库中 tb_user 补全 tb_user_info（仅插入尚不存在的 user_id，可重复执行）。
  适用：曾导入旧版 hmdp.sql（无 user_info 数据）或只导了用户表的情况。
  若已用新版 hmdp.sql 初始化，本脚本会因 NOT EXISTS 跳过，无影响。
*/
SET NAMES utf8mb4;

INSERT INTO `tb_user_info` (`user_id`, `city`, `introduce`, `fans`, `followee`, `gender`, `birthday`, `credits`, `level`, `hide_following`)
SELECT
  u.`id`,
  ELT(1 + (u.`id` % 5), '杭州', '上海', '北京', '深圳', '成都'),
  LEFT(CONCAT('探店与笔记测试账号 · ', IFNULL(NULLIF(TRIM(u.`nick_name`), ''), CONCAT('用户', u.`id`)), ' · 欢迎关注。'), 128),
  ((u.`id` * 13 + 7) % 500),
  ((u.`id` * 5 + 3) % 80),
  (u.`id` % 2),
  DATE_ADD('1992-01-01', INTERVAL ((u.`id` * 37) % 4000) DAY),
  ((u.`id` * 19) % 2000),
  (u.`id` % 4),
  IF((u.`id` % 17) = 0, 1, 0)
FROM `tb_user` u
WHERE NOT EXISTS (SELECT 1 FROM `tb_user_info` i WHERE i.`user_id` = u.`id`);
