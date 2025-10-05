/*
 Navicat Premium Dump SQL

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : trip

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 27/09/2025 14:58:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_blog
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog`;
CREATE TABLE `tb_blog`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `scenic_id` bigint NOT NULL COMMENT '关联的景区id',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `images` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '游记的照片，最多9张，多张以\",\"隔开',
  `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '游记的文字描述',
  `liked` int UNSIGNED NULL DEFAULT 0 COMMENT '点赞数量',
  `comments` int UNSIGNED NULL DEFAULT NULL COMMENT '评论数量',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Table structure for tb_blog_comments
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog_comments`;
CREATE TABLE `tb_blog_comments`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户id',
  `blog_id` bigint UNSIGNED NOT NULL COMMENT '景点评价id',
  `parent_id` bigint UNSIGNED NOT NULL COMMENT '关联的1级评论id，如果是一级评论值为0',
  `answer_id` bigint UNSIGNED NOT NULL COMMENT '回复的评论id',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '回复的内容',
  `liked` int UNSIGNED NULL DEFAULT NULL COMMENT '点赞数',
  `status` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '状态，0：正常，1：被举报，2：禁止查看',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_blog_comments
-- ----------------------------

-- ----------------------------
-- Table structure for tb_follow
-- ----------------------------
DROP TABLE IF EXISTS `tb_follow`;
CREATE TABLE `tb_follow`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户id',
  `follow_user_id` bigint UNSIGNED NOT NULL COMMENT '关联的用户id',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_follow
-- ----------------------------

-- ----------------------------
-- Table structure for tb_scenic
-- ----------------------------
DROP TABLE IF EXISTS `tb_scenic`;
CREATE TABLE `tb_scenic`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '景区名称',
  `type_id` bigint UNSIGNED NOT NULL COMMENT '分类id',
  `images` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片，多个以\",\"隔开',
  `area` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '区域，例如陆家嘴',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地址',
  `x` double UNSIGNED NOT NULL COMMENT '经度',
  `y` double UNSIGNED NOT NULL COMMENT '维度',
  `avg_price` bigint UNSIGNED NULL DEFAULT NULL COMMENT '均价，取整数',
  `sold` int UNSIGNED NOT NULL COMMENT '销量',
  `comments` int UNSIGNED NOT NULL COMMENT '评论数量',
  `score` int UNSIGNED NOT NULL COMMENT '评分，1~5分，乘10保存，避免小数',
  `open_hours` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开放时间，例如 10:00-22:00',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `foreign_key_type`(`type_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 251 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_scenic
-- ----------------------------
INSERT INTO `tb_scenic` VALUES (1, '南宫鹦鹉园', 2, 'http://store.is.autonavi.com/showpic/ee58987a5febb3ec5051d80976f12be7,http://store.is.autonavi.com/showpic/8b4a82edcbd6e2ac248256faf349e482,https://aos-comment.amap.com/default_poi/comment/f215b801ce0f6931c931790703cf9684_2048_2048_80.jpg', '丰台区', '王佐镇南宫迎宾路1号', 116.152995, 39.796006, 211, 49733, 8792, 44, '08:30-17:00', '2024-01-10 10:21:06', '2025-09-27 14:20:24');
INSERT INTO `tb_scenic` VALUES (2, '故宫博物院-钟表馆', 1, 'http://store.is.autonavi.com/showpic/db50433739ce4770509508b3bfe92550,http://store.is.autonavi.com/showpic/a2f9feafb7bda59a48d1bad56acb0d4a,https://aos-comment.amap.com/B000A9LFO0/comment/7e7a7a220a30216d2c0d849d0319af1c_2048_2048_80.jpg', '东城区', '景山前街4号故宫博物院', 116.399152, 39.918847, 199, 31294, 10952, 43, '全天开放', '2024-10-21 21:57:37', '2023-01-25 08:18:10');
INSERT INTO `tb_scenic` VALUES (3, '玉渊潭公园钓鱼台银杏大道', 1, 'http://store.is.autonavi.com/showpic/0f78a60ee7798941730ef34d67c2f6ab,http://store.is.autonavi.com/showpic/62d0c266b5186b7ec428ac8ccd2317f3,http://store.is.autonavi.com/showpic/4bccc44c0a2a425108b348fe71991f1d', '海淀区', '三里河路钓鱼台国宾馆东墙外(钓鱼台国宾馆东侧路)', 116.334236, 39.918357, 73, 33234, 4674, 47, '08:30-17:00', '2024-11-04 16:23:03', '2023-12-10 22:28:21');
INSERT INTO `tb_scenic` VALUES (4, '奥林匹克森林公园南园', 6, 'http://store.is.autonavi.com/showpic/dd37ce835b65616b7231c857d0e76d2b,http://store.is.autonavi.com/showpic/74155819060ca0999d720b4c76fcdfc3,http://store.is.autonavi.com/showpic/c2e1e0107ae82b0b1bb013dd823c1d08', '朝阳区', '北五环辅路奥林匹克森林公园', 116.391365, 40.016194, 89, 5411, 1774, 48, '08:00-17:30', '2024-02-26 16:32:39', '2024-07-14 20:47:37');
INSERT INTO `tb_scenic` VALUES (5, '天开寺', 4, 'http://store.is.autonavi.com/showpic/f800149e54e2b9ecc93d1dd8292f805c,http://store.is.autonavi.com/showpic/6c24e6a69a24c11f38219496c407fe9f,https://aos-comment.amap.com/B000A8VW5H/comment/content_media_external_file_67078_ss__1762383298396_34775264.jpg', '房山区', '韩村河', 115.896205, 39.618233, 60, 6804, 3093, 46, '全天开放', '2023-05-07 19:27:12', '2024-02-22 09:32:46');
INSERT INTO `tb_scenic` VALUES (6, '中门寺生态园', 1, 'https://aos-comment.amap.com/B000A8U0NN/comment/ef33ecfbe2a555f386b4727d5246caf2_2048_2048_80.jpg,https://aos-comment.amap.com/B000A8U0NN/comment/46d0cf75af921657fa8fb64e0f507e2c_2048_2048_80.jpg,https://aos-comment.amap.com/B000A8U0NN/comment/10f8b621b8b8e6617c3fc9242d10e2c0_2048_2048_80.jpg', '门头沟区', '中门寺街71号', 116.082498, 39.927887, 143, 27030, 2798, 44, '08:30-17:00', '2023-05-24 18:30:36', '2024-04-11 16:17:03');

-- ----------------------------
-- Table structure for tb_scenic_type
-- ----------------------------
DROP TABLE IF EXISTS `tb_scenic_type`;
CREATE TABLE `tb_scenic_type`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int UNSIGNED NULL DEFAULT NULL COMMENT '顺序',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_scenic_type
-- ----------------------------
INSERT INTO `tb_scenic_type` VALUES (1, '风景名胜', 'icon_01.png', 1, '2024-12-09 14:01:00', '2025-09-09 19:59:21');
INSERT INTO `tb_scenic_type` VALUES (2, '旅游景点', 'icon_02.png', 2, '2024-12-09 14:01:00', '2025-09-09 19:59:23');
INSERT INTO `tb_scenic_type` VALUES (3, '公园', 'icon_03.png', 3, '2024-12-09 14:01:00', '2025-09-09 19:59:23');
INSERT INTO `tb_scenic_type` VALUES (4, '寺庙道观', 'icon_04.png', 4, '2024-12-09 14:01:00', '2025-09-09 19:59:24');
INSERT INTO `tb_scenic_type` VALUES (5, '城市广场', 'icon_05.png', 5, '2024-12-09 14:01:00', '2025-09-09 19:59:26');
INSERT INTO `tb_scenic_type` VALUES (6, '国家级景点', 'icon_06.png', 6, '2024-12-09 14:01:00', '2025-09-09 19:59:26');
INSERT INTO `tb_scenic_type` VALUES (7, '回教寺', 'icon_07.png', 7, '2024-12-09 14:01:00', '2025-09-09 19:59:27');
INSERT INTO `tb_scenic_type` VALUES (8, '省级景点', 'icon_08.png', 8, '2024-12-09 14:01:00', '2025-09-09 19:59:29');
INSERT INTO `tb_scenic_type` VALUES (9, '教堂', 'icon_09.png', 9, '2024-12-09 14:01:00', '2025-09-09 19:59:31');
INSERT INTO `tb_scenic_type` VALUES (10, '植物园', 'icon_10.png', 10, '2024-12-09 14:01:00', '2025-09-09 19:59:33');
INSERT INTO `tb_scenic_type` VALUES (11, '动物园', 'icon_11.png', 11, '2024-12-09 14:01:00', '2025-09-09 19:59:35');
INSERT INTO `tb_scenic_type` VALUES (12, '公园广场', 'icon_12.png', 12, '2024-12-09 14:01:00', '2025-09-09 19:59:36');
INSERT INTO `tb_scenic_type` VALUES (13, '世界遗产', 'icon_13.png', 13, '2024-12-09 14:01:00', '2025-09-09 19:59:37');
INSERT INTO `tb_scenic_type` VALUES (14, '纪念馆', 'icon_14.png', 14, '2024-12-09 14:01:00', '2025-09-09 19:59:38');
INSERT INTO `tb_scenic_type` VALUES (15, '红色景区', 'icon_15.png', 15, '2024-12-09 14:01:00', '2025-09-09 19:59:39');
INSERT INTO `tb_scenic_type` VALUES (16, '观景点', 'icon_16.png', 16, '2024-12-09 14:01:00', '2025-09-09 19:59:42');

-- ----------------------------
-- Table structure for tb_seckill_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_seckill_order`;
CREATE TABLE `tb_seckill_order`  (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '下单的用户id',
  `voucher_id` bigint UNSIGNED NOT NULL COMMENT '购买凭证id',
  `pay_type` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '支付方式 1：余额支付；2：支付宝；3：微信',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态，1：未支付；2：已支付；3：已核销；4：已取消；5：退款中；6：已退款',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `pay_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `use_time` timestamp NULL DEFAULT NULL COMMENT '核销时间',
  `refund_time` timestamp NULL DEFAULT NULL COMMENT '退款时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_voucher`(`user_id` ASC, `voucher_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_seckill_order
-- ----------------------------

-- ----------------------------
-- Table structure for tb_seckill_voucher
-- ----------------------------
DROP TABLE IF EXISTS `tb_seckill_voucher`;
CREATE TABLE `tb_seckill_voucher`  (
  `voucher_id` bigint UNSIGNED NOT NULL COMMENT '关联的凭证id',
  `stock` int NOT NULL COMMENT '库存',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `begin_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
  `end_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '失效时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`voucher_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '秒杀凭证表，与凭证是一对一关系' ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_seckill_voucher
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号码',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码，加密存储',
  `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '昵称，默认是用户id',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '人物头像',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniqe_key_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1012 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_user
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_info`;
CREATE TABLE `tb_user_info`  (
  `user_id` bigint UNSIGNED NOT NULL COMMENT '主键，用户id',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '所在城市',
  `introduce` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个人介绍，不要超过128个字符',
  `fans` int UNSIGNED NULL DEFAULT 0 COMMENT '粉丝数量',
  `followee` int UNSIGNED NULL DEFAULT 0 COMMENT '关注的人的数量',
  `gender` tinyint UNSIGNED NULL DEFAULT 0 COMMENT '性别，0：男，1：女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `credits` int UNSIGNED NULL DEFAULT 0 COMMENT '积分',
  `level` tinyint UNSIGNED NULL DEFAULT 0 COMMENT '等级',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_user_info
-- ----------------------------

-- ----------------------------
-- Table structure for tb_voucher
-- ----------------------------
DROP TABLE IF EXISTS `tb_voucher`;
CREATE TABLE `tb_voucher`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `scenic_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '景区id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '凭证标题',
  `sub_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '副标题',
  `rules` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '使用规则',
  `pay_value` bigint UNSIGNED NOT NULL COMMENT '支付金额，单位是分',
  `actual_value` bigint NOT NULL COMMENT '实际金额，单位是分',
  `type` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '0,普通凭证；1,秒杀凭证',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '1,上架; 2,下架; 3,过期',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_voucher
-- ----------------------------

-- ----------------------------
-- Table structure for tb_voucher_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_voucher_order`;
CREATE TABLE `tb_voucher_order`  (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '下单的用户id',
  `voucher_id` bigint UNSIGNED NOT NULL COMMENT '购买的凭证id',
  `pay_type` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '支付方式 1：余额支付；2：支付宝；3：微信',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态，1：未支付；2：已支付；3：已核销；4：已取消；5：退款中；6：已退款',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `pay_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `use_time` timestamp NULL DEFAULT NULL COMMENT '核销时间',
  `refund_time` timestamp NULL DEFAULT NULL COMMENT '退款时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_voucher_order
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
