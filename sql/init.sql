/*
 Navicat Premium Data Transfer

 Source Server         : capmus
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : localhost:3306
 Source Schema         : campus

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 26/05/2026 00:02:35
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `activity_id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '活动描述',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动封面图',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `tag_id` bigint NOT NULL COMMENT '关联的活动标签ID',
  `participant_count` int NULL DEFAULT 0 COMMENT '参与人数',
  `post_count` int NULL DEFAULT 0 COMMENT '帖子数量',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-进行中,0-已结束',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`activity_id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_tag`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner`  (
  `banner_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Banner ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接',
  `facility_id` bigint NULL DEFAULT NULL COMMENT '跳转设施ID',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`banner_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Banner轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for comment_like
-- ----------------------------
DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like`  (
  `like_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `comment_id` bigint NOT NULL COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`like_id`) USING BTREE,
  UNIQUE INDEX `uk_comment_user`(`comment_id` ASC, `user_id` ASC) USING BTREE COMMENT '防止重复点赞',
  INDEX `idx_comment_id`(`comment_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for comment_vote
-- ----------------------------
DROP TABLE IF EXISTS `comment_vote`;
CREATE TABLE `comment_vote`  (
  `vote_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `comment_id` bigint NOT NULL COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `vote_type` tinyint NOT NULL COMMENT '投票类型：1-点赞，0-点踩',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`vote_id`) USING BTREE,
  UNIQUE INDEX `uk_comment_user`(`comment_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_comment_id`(`comment_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论投票表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for daily_post
-- ----------------------------
DROP TABLE IF EXISTS `daily_post`;
CREATE TABLE `daily_post`  (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `mood` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '心情表情',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '帖子标题',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分享内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图片URL列表（JSON格式）',
  `tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '标签列表（JSON格式）',
  `topic_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '关联话题ID列表JSON',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int NULL DEFAULT 0 COMMENT '评论数',
  `collect_count` int NULL DEFAULT 0 COMMENT '收藏数',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览数',
  `hot_score` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '热度分',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-正常，0-已删除，2-待审核',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`post_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_hot`(`hot_score` DESC) USING BTREE,
  INDEX `idx_create`(`create_time` DESC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '日常分享表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for evaluation
-- ----------------------------
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation`  (
  `evaluation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `facility_id` bigint NOT NULL COMMENT '设施ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `rating` int NOT NULL COMMENT '评分1-5星',
  `dimension_scores` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '多维度评分JSON: {\"taste\":4,\"environment\":5,\"service\":3}',
  `mood` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '心情表情',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图片URL列表（JSON格式）',
  `tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '标签列表（JSON格式）',
  `visit_time` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '到访时段：breakfast/lunch/dinner',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int NULL DEFAULT 0 COMMENT '评论数',
  `helpful_count` int NULL DEFAULT 0 COMMENT '有用数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-正常，0-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`evaluation_id`) USING BTREE,
  INDEX `idx_facility_id`(`facility_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 57 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for evaluation_backup
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_backup`;
CREATE TABLE `evaluation_backup`  (
  `evaluation_id` bigint NOT NULL DEFAULT 0 COMMENT '主键ID',
  `facility_id` bigint NULL DEFAULT NULL COMMENT '设施ID（仅设施评价时有值）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'EVALUATION' COMMENT '内容类型：EVALUATION-设施评价, POST-日常分享',
  `rating` int NULL DEFAULT NULL COMMENT '评分1-5星（仅设施评价时有值）',
  `mood` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '心情表情',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图片URL列表（JSON格式）',
  `tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '标签列表（JSON格式）',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int NULL DEFAULT 0 COMMENT '评论数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-正常，0-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for evaluation_comment
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_comment`;
CREATE TABLE `evaluation_comment`  (
  `comment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `evaluation_id` bigint NOT NULL COMMENT '评价ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父评论ID',
  `reply_to_user_id` bigint NULL DEFAULT NULL COMMENT '回复的用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-正常，0-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图片URL列表（JSON格式）',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `dislike_count` int NULL DEFAULT 0 COMMENT '点踩数',
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `idx_evaluation_id`(`evaluation_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 63 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for evaluation_helpful
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_helpful`;
CREATE TABLE `evaluation_helpful`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `evaluation_id` bigint NOT NULL COMMENT '评价ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `vote_type` tinyint NOT NULL COMMENT '投票类型：1-有用，0-无用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_eval_user`(`evaluation_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_evaluation`(`evaluation_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价有用性投票表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for evaluation_like
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_like`;
CREATE TABLE `evaluation_like`  (
  `like_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `evaluation_id` bigint NOT NULL COMMENT '评价ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`like_id`) USING BTREE,
  UNIQUE INDEX `uk_evaluation_user`(`evaluation_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 75 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for evaluation_tag_relation
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_tag_relation`;
CREATE TABLE `evaluation_tag_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `evaluation_id` bigint NOT NULL COMMENT '评价ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_eval_tag`(`evaluation_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_tag`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设施评价标签关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for facility
-- ----------------------------
DROP TABLE IF EXISTS `facility`;
CREATE TABLE `facility`  (
  `facility_id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设施名称',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '位置描述',
  `rating` decimal(3, 1) NULL DEFAULT 0.0 COMMENT '综合评分（1-5分）',
  `review_count` int NULL DEFAULT 0 COMMENT '评价总数',
  `campus` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属校区',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图',
  `detail_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详细介绍（如营业时间、特色菜）',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-营业中, 0-已下架/删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_rank` int NULL DEFAULT 0 COMMENT '上次排名（用于计算趋势）',
  `rank_update_time` datetime NULL DEFAULT NULL COMMENT '排名更新时间',
  PRIMARY KEY (`facility_id`) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE,
  CONSTRAINT `fk_facility_category` FOREIGN KEY (`category_id`) REFERENCES `facility_category` (`category_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设施表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for facility_category
-- ----------------------------
DROP TABLE IF EXISTS `facility_category`;
CREATE TABLE `facility_category`  (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称，如：食堂、教学楼',
  `icon_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类图标URL',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类图标URL',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序权重',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设施分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for facility_reply
-- ----------------------------
DROP TABLE IF EXISTS `facility_reply`;
CREATE TABLE `facility_reply`  (
  `reply_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `evaluation_id` bigint NOT NULL COMMENT '评价ID',
  `admin_id` bigint NOT NULL COMMENT '管理员ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '回复内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`reply_id`) USING BTREE,
  INDEX `idx_evaluation`(`evaluation_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设施官方回复表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for post_collect
-- ----------------------------
DROP TABLE IF EXISTS `post_collect`;
CREATE TABLE `post_collect`  (
  `collect_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `folder` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'default' COMMENT '收藏夹名称',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`collect_id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for post_comment
-- ----------------------------
DROP TABLE IF EXISTS `post_comment`;
CREATE TABLE `post_comment`  (
  `comment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父评论ID',
  `reply_to_user_id` bigint NULL DEFAULT NULL COMMENT '被回复用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-正常，0-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `idx_post`(`post_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for post_like
-- ----------------------------
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like`  (
  `like_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`like_id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `user_id` ASC) USING BTREE COMMENT '防止重复点赞',
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子点赞表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for post_tag_relation
-- ----------------------------
DROP TABLE IF EXISTS `post_tag_relation`;
CREATE TABLE `post_tag_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_tag`(`post_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_tag`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '日常帖子标签关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for post_topic
-- ----------------------------
DROP TABLE IF EXISTS `post_topic`;
CREATE TABLE `post_topic`  (
  `topic_id` bigint NOT NULL AUTO_INCREMENT COMMENT '话题ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '话题名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '话题图标',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '话题描述',
  `post_count` int NULL DEFAULT 0 COMMENT '帖子数量',
  `follow_count` int NULL DEFAULT 0 COMMENT '关注人数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`topic_id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '话题表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人昵称（冗余字段，方便查看）',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作模块，如：设施管理、用户管理',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型，如：DELETE, UPDATE, ADD',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法名',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作IP',
  `status` tinyint NULL DEFAULT 1 COMMENT '操作状态：1-成功, 0-失败',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息（如果失败）',
  `cost_time` bigint NULL DEFAULT 0 COMMENT '耗时(毫秒)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `publish_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `is_top` tinyint NULL DEFAULT 0 COMMENT '是否置顶',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '微信OpenID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '微信用户' COMMENT '昵称',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `role` enum('USER','ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'USER' COMMENT '角色：USER-普通用户, ADMIN-管理员',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-正常, 0-封禁',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login_time` datetime NULL DEFAULT NULL,
  `signature` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个性签名',
  `gender` int NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `openid`(`openid` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `tag_id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `tag_type` tinyint NULL DEFAULT 1 COMMENT '标签类型：1-普通标签,2-活动标签',
  `activity_id` bigint NULL DEFAULT NULL COMMENT '关联活动ID（活动标签时有效）',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签图标',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签颜色',
  `use_count` int NULL DEFAULT 0 COMMENT '使用次数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用,0-禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tag_id`) USING BTREE,
  UNIQUE INDEX `uk_tag_name`(`tag_name` ASC) USING BTREE,
  INDEX `idx_type`(`tag_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一标签表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for topic_follow
-- ----------------------------
DROP TABLE IF EXISTS `topic_follow`;
CREATE TABLE `topic_follow`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `topic_id` bigint NOT NULL COMMENT '话题ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_topic_user`(`topic_id` ASC, `user_id` ASC) USING BTREE COMMENT '防止重复关注',
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '话题关注表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite`  (
  `favorite_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `facility_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`favorite_id`) USING BTREE,
  UNIQUE INDEX `uk_user_facility`(`user_id` ASC, `facility_id` ASC) USING BTREE,
  INDEX `idx_facility`(`facility_id` ASC) USING BTREE,
  CONSTRAINT `fk_fav_facility` FOREIGN KEY (`facility_id`) REFERENCES `facility` (`facility_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_notification
-- ----------------------------
DROP TABLE IF EXISTS `user_notification`;
CREATE TABLE `user_notification`  (
  `notification_id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `from_user_id` bigint NULL DEFAULT NULL COMMENT '发送用户ID（系统通知为NULL）',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知类型：LIKE-点赞,COMMENT-评论,REPLY-回复,MENTION-@提及,OFFICIAL_REPLY-官方回复,SYSTEM-系统通知',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID（评价ID/帖子ID/评论ID）',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联类型：EVALUATION-评价,POST-帖子,COMMENT-评论',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知内容',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '是否已读：0-未读,1-已读',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`notification_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_read`(`user_id` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户消息通知表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
