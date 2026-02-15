/*
Navicat MySQL Data Transfer

Source Server         : Moyii
Source Server Version : 50724
Source Host           : 127.0.0.1:3306
Source Database       : lz_sports

Target Server Type    : MYSQL
Target Server Version : 50724
File Encoding         : 65001

Date: 2024-01-12 21:02:12
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for athlete
-- ----------------------------
DROP TABLE IF EXISTS `athlete`;
CREATE TABLE `athlete` (
  `AthleteID` int(11) NOT NULL AUTO_INCREMENT COMMENT '运动员ID',
  `UserID` int(11) DEFAULT NULL COMMENT '用户ID',
  `Name` varchar(50) COLLATE utf8mb4_german2_ci NOT NULL COMMENT '姓名',
  `Age` int(11) DEFAULT NULL COMMENT '年龄',
  `Gender` varchar(10) COLLATE utf8mb4_german2_ci DEFAULT NULL COMMENT '性别',
  `Contact` varchar(50) COLLATE utf8mb4_german2_ci DEFAULT NULL COMMENT '联系方式',
  `AthleteState` varchar(20) COLLATE utf8mb4_german2_ci DEFAULT '在审核' COMMENT '运动员审核状态',
  `applyTime` datetime NOT NULL DEFAULT '2023-11-10 02:10:00' COMMENT '申请时间',
  `agreeTime` datetime DEFAULT NULL COMMENT '同意时间',
  `grade` varchar(20) COLLATE utf8mb4_german2_ci DEFAULT NULL COMMENT '学生年级',
  PRIMARY KEY (`AthleteID`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_german2_ci;

-- ----------------------------
-- Records of athlete
-- ----------------------------
INSERT INTO `athlete` VALUES ('28', '14', '李四', '20', '男', '15619763223', '成功', '2024-01-04 01:01:11', '2024-01-04 01:06:51', '九年级');
INSERT INTO `athlete` VALUES ('29', '13', '刘震', '20', '男', '15619763223', '成功', '2024-01-04 17:18:44', '2024-01-04 17:19:10', '六年级');
INSERT INTO `athlete` VALUES ('30', '15', '王五', '20', '男', '15619763223', '成功', '2024-01-05 00:32:44', '2024-01-09 12:47:47', '二年级');
INSERT INTO `athlete` VALUES ('32', '12', '吴晨浩', '18', '男', '15619763223', '成功', '2024-01-05 16:15:57', '2024-01-05 16:16:15', '八年级');
INSERT INTO `athlete` VALUES ('33', '17', '万牧', '20', '男', '15619763223', '成功', '2024-01-09 12:47:35', '2024-01-09 12:47:50', '八年级');
INSERT INTO `athlete` VALUES ('34', '18', '万路', '20', '男', '15619763223', '成功', '2024-01-09 13:37:19', '2024-01-09 14:09:36', '八年级');
INSERT INTO `athlete` VALUES ('35', '19', '刘毅', '20', '男', '15619763223', '不同意', '2024-01-09 14:21:27', null, '八年级');

-- ----------------------------
-- Table structure for event
-- ----------------------------
DROP TABLE IF EXISTS `event`;
CREATE TABLE `event` (
  `EventID` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `EventName` varchar(50) COLLATE utf8mb4_german2_ci NOT NULL COMMENT '活动名称',
  `RegistrationStart` datetime DEFAULT NULL COMMENT '报名开始时间',
  `RegistrationFee` decimal(10,2) NOT NULL COMMENT '报名费用',
  `Eligibility` varchar(100) COLLATE utf8mb4_german2_ci DEFAULT NULL COMMENT '参赛方式',
  `RegistrationEnd` datetime DEFAULT NULL COMMENT '报名截止时间',
  PRIMARY KEY (`EventID`),
  UNIQUE KEY `EventName` (`EventName`)
) ENGINE=InnoDB AUTO_INCREMENT=98 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_german2_ci;

-- ----------------------------
-- Records of event
-- ----------------------------
INSERT INTO `event` VALUES ('94', '夏季运动会', '2024-01-03 16:00:00', '0.00', '线上报名', '2024-01-10 16:00:00');
INSERT INTO `event` VALUES ('95', '秋季运动会', '2024-01-02 16:00:00', '0.00', '线下报名', '2024-01-09 16:00:00');
INSERT INTO `event` VALUES ('96', '春季运动会', '2024-01-02 19:33:23', '0.00', '线下报名', '2024-01-08 19:33:50');
INSERT INTO `event` VALUES ('97', '24年运动会', '2024-01-03 03:34:33', '0.00', '单位报名', '2024-01-09 03:35:19');

-- ----------------------------
-- Table structure for eventitem
-- ----------------------------
DROP TABLE IF EXISTS `eventitem`;
CREATE TABLE `eventitem` (
  `ItemID` int(11) NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `EventID` int(11) DEFAULT NULL COMMENT '活动ID',
  `ItemName` varchar(50) COLLATE utf8mb4_german2_ci NOT NULL COMMENT '项目名称',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  `grade` varchar(20) COLLATE utf8mb4_german2_ci DEFAULT NULL COMMENT '项目所属年级',
  `limitation` varchar(20) COLLATE utf8mb4_german2_ci DEFAULT NULL COMMENT '报名限制',
  `start` datetime DEFAULT NULL COMMENT '项目开始时间',
  `end` datetime DEFAULT NULL COMMENT '项目结束时间',
  `attendance` int(11) DEFAULT '0' COMMENT '参加项目人数',
  `maxAttendance` int(11) DEFAULT '20' COMMENT '最大参加人数',
  PRIMARY KEY (`ItemID`)
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_german2_ci;

-- ----------------------------
-- Records of eventitem
-- ----------------------------
INSERT INTO `eventitem` VALUES ('31', '94', '跳高', '2024-01-03 20:14:40', '八年级', '男', '2024-01-07 07:19:27', '2024-01-11 19:19:30', '2', '10');
INSERT INTO `eventitem` VALUES ('32', '95', '短跑400米', '2024-01-03 22:34:26', '八年级', '男', '2024-01-07 17:19:27', '2024-01-11 19:19:30', '0', '20');
INSERT INTO `eventitem` VALUES ('34', '94', '三级跳远', '2024-01-04 19:04:28', '五年级', '男', '2024-01-07 08:19:27', '2024-01-11 19:19:30', '0', '20');
INSERT INTO `eventitem` VALUES ('35', '94', '跳高', '2024-01-04 19:09:14', '八年级', '女', '2024-01-07 09:19:27', '2024-01-11 19:19:30', '0', '10');
INSERT INTO `eventitem` VALUES ('36', '94', '铅球', '2024-01-04 19:12:58', '八年级', '男', '2024-01-07 11:19:27', '2024-01-11 16:00:00', '1', '5');
INSERT INTO `eventitem` VALUES ('37', '97', '短跑100米', '2024-01-04 21:25:27', '八年级', '男', '2024-01-07 12:19:27', '2024-01-11 16:00:00', '0', '20');
INSERT INTO `eventitem` VALUES ('38', '96', '4x100米', '2024-01-04 21:26:33', '九年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '20');
INSERT INTO `eventitem` VALUES ('39', '94', '100米', '2024-01-04 21:27:13', '三年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '10');
INSERT INTO `eventitem` VALUES ('40', '94', '跳高', '2024-01-04 21:51:29', '八年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '1', '20');
INSERT INTO `eventitem` VALUES ('41', '95', '跳远', '2024-01-03 22:34:26', '八年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '20');
INSERT INTO `eventitem` VALUES ('42', '94', '跳高', '2024-01-04 21:51:29', '八年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '13');
INSERT INTO `eventitem` VALUES ('43', '95', '跳远', '2024-01-03 22:34:26', '八年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '20');
INSERT INTO `eventitem` VALUES ('44', '95', '跳远', '2024-01-03 22:34:26', '八年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '20');
INSERT INTO `eventitem` VALUES ('45', '95', '跳远', '2024-01-03 22:34:26', '八年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '20');
INSERT INTO `eventitem` VALUES ('46', '94', '跳高', '2024-01-04 21:51:29', '八年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '13');
INSERT INTO `eventitem` VALUES ('47', '95', '跳远', '2024-01-03 22:34:26', '八年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '1', '20');
INSERT INTO `eventitem` VALUES ('49', '94', '4x100米', '2024-01-04 21:51:29', '八年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '10');
INSERT INTO `eventitem` VALUES ('51', '94', '铅球', '2024-01-04 21:51:29', '八年级', '女', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '10');
INSERT INTO `eventitem` VALUES ('56', '95', '跳远', '2024-01-03 22:34:26', '八年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '20');
INSERT INTO `eventitem` VALUES ('57', '95', '跳远', '2024-01-03 22:34:26', '八年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '20');
INSERT INTO `eventitem` VALUES ('58', '94', '跳高', '2024-01-04 21:51:29', '八年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '1', '20');
INSERT INTO `eventitem` VALUES ('59', '95', '跳远', '2024-01-03 22:34:26', '八年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '20');
INSERT INTO `eventitem` VALUES ('61', '95', '跳远', '2024-01-03 22:34:26', '七年级', '男', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '20');
INSERT INTO `eventitem` VALUES ('62', '97', '100米', '2024-01-05 09:52:28', '七年级', '女', '2024-01-07 19:19:27', '2024-01-11 19:19:30', '0', '10');

-- ----------------------------
-- Table structure for registration
-- ----------------------------
DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration` (
  `RegistrationID` int(11) NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `AthleteID` int(11) NOT NULL COMMENT '运动员ID',
  `EventID` int(11) NOT NULL COMMENT '活动ID',
  `ItemID` int(11) NOT NULL COMMENT '项目ID',
  `RegistrationTime` datetime NOT NULL COMMENT '报名时间',
  `RegistrationStatus` varchar(20) COLLATE utf8mb4_german2_ci DEFAULT '审核中' COMMENT '报名状态',
  PRIMARY KEY (`RegistrationID`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_german2_ci;

-- ----------------------------
-- Records of registration
-- ----------------------------
INSERT INTO `registration` VALUES ('58', '33', '94', '58', '2024-01-09 13:05:43', '通过');
INSERT INTO `registration` VALUES ('59', '33', '94', '36', '2024-01-09 13:06:14', '未通过');
INSERT INTO `registration` VALUES ('60', '33', '94', '31', '2024-01-09 13:06:28', '通过');
INSERT INTO `registration` VALUES ('61', '33', '94', '40', '2024-01-09 13:11:14', '审核中');
INSERT INTO `registration` VALUES ('62', '32', '94', '31', '2024-01-09 14:37:11', '审核中');
INSERT INTO `registration` VALUES ('63', '32', '94', '36', '2024-01-09 14:37:13', '通过');

-- ----------------------------
-- Table structure for sportsimg
-- ----------------------------
DROP TABLE IF EXISTS `sportsimg`;
CREATE TABLE `sportsimg` (
  `ImgId` int(11) NOT NULL AUTO_INCREMENT COMMENT '图片id',
  `ImgType` varchar(20) COLLATE utf8mb4_german2_ci NOT NULL COMMENT '图片类型',
  `typeId` int(11) NOT NULL COMMENT '图片相关ID',
  `ImgSrc` varchar(50) COLLATE utf8mb4_german2_ci DEFAULT NULL COMMENT '图片地址',
  PRIMARY KEY (`ImgId`)
) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_german2_ci;

-- ----------------------------
-- Records of sportsimg
-- ----------------------------
INSERT INTO `sportsimg` VALUES ('69', '活动图片', '1', '1bedab37-1256-4254-85d1-1ee8c34a30bc-image0.png');
INSERT INTO `sportsimg` VALUES ('70', '活动图片', '1', '892245b3-d540-470a-87b9-f392cead5379-image0.png');
INSERT INTO `sportsimg` VALUES ('73', '项目图片', '1', '7a51bb6a-42eb-4706-ace6-d039480bfea3-image0.png');
INSERT INTO `sportsimg` VALUES ('77', '项目图片', '1', '09212f2b-7162-4a71-ab76-f8642377f3d1-image1.png');
INSERT INTO `sportsimg` VALUES ('82', '项目图片', '38', '8afc3b24-1211-4775-81e5-5059ab6bbd62-image0.png');
INSERT INTO `sportsimg` VALUES ('83', '项目图片', '31', 'ee5ee8eb-6cb3-46f4-925c-50b0767a9db2-image0.png');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `UserID` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 用户ID',
  `Username` varchar(50) COLLATE utf8mb4_german2_ci NOT NULL COMMENT '用户名',
  `Password` varchar(50) COLLATE utf8mb4_german2_ci NOT NULL COMMENT '密码',
  `UserType` varchar(20) COLLATE utf8mb4_german2_ci NOT NULL COMMENT '用户类型：运动员或工作人员',
  `Status` varchar(20) COLLATE utf8mb4_german2_ci NOT NULL DEFAULT '未启用' COMMENT '是否激活',
  `Email` varchar(20) COLLATE utf8mb4_german2_ci NOT NULL COMMENT '激活邮箱',
  `registerTime` datetime NOT NULL DEFAULT '2023-11-10 02:10:00' COMMENT '注册时间',
  PRIMARY KEY (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_german2_ci;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', 'lz1223', '2312034544lz', '工作人员', '已激活', '2312034544@qq.com', '2023-11-10 02:10:00');
INSERT INTO `user` VALUES ('12', 'player', '2312034544lz', '运动员', '已激活', '2312034544@qq.com', '2024-01-03 16:18:32');
INSERT INTO `user` VALUES ('13', 'zhangsan', '2312034544lz', '运动员', '已激活', '2312034544@qq.com', '2024-01-03 22:47:28');
INSERT INTO `user` VALUES ('14', 'lisi', '2312034544lz', '运动员', '已激活', '2312034544@qq.com', '2024-01-04 00:54:31');
INSERT INTO `user` VALUES ('15', 'wanwu', '2312034544lz', '运动员', '已激活', '2312034544@qq.com', '2023-11-10 02:10:00');
INSERT INTO `user` VALUES ('16', 'wanhua', '2312034544', '学生', '已激活', '2312034544@qq.com', '2024-01-03 01:10:00');
INSERT INTO `user` VALUES ('17', 'wanmu', '2312034544lz', '运动员', '已激活', '2312034544@qq.com', '2023-11-10 02:10:00');
INSERT INTO `user` VALUES ('18', 'wanlu', '2312034544lz', '运动员', '已激活', '2312034544@qq.com', '2023-11-10 02:10:00');
INSERT INTO `user` VALUES ('19', 'liuyi', '2312034544lz', '学生', '已激活', '2312034544@qq.com', '2023-11-10 02:10:00');
