--;; 公司信息
CREATE TABLE `t_company` (
	`company_id` varchar(40) NOT NULL COMMENT '主键',
	`company_name` varchar(100) NOT NULL COMMENT '公司名称',
  `company_store_name` varchar(500) NOT NULL DEFAULT '' COMMENT '公司门店名称',
	`company_no` varchar(40) DEFAULT NULL COMMENT '公司编码',
	`company_logo` varchar(100) DEFAULT NULL COMMENT '公司logo',
	`company_url` varchar(100) DEFAULT NULL COMMENT '公司网站',
	`company_intro` varchar(400) DEFAULT NULL COMMENT '公司简介',
	`company_index_url` varchar(100) DEFAULT NULL COMMENT '公司首页图片地址',
	`remark` varchar(400) DEFAULT NULL COMMENT '备注',
	`delete_flag` varchar(4) DEFAULT '0' COMMENT '删除标记 0：未删除 1：已删除',
	`create_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`create_user_id` varchar(40) DEFAULT NULL COMMENT '创建人id',
  `update_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `update_user_id` varchar(40) DEFAULT NULL COMMENT '最后修改人id',
  `ali_private_key` text DEFAULT NULL COMMENT '支付宝私钥',
  `ali_public_key` text DEFAULT NULL COMMENT '支付宝公钥',
  `ali_app_id` varchar(40) DEFAULT NULL COMMENT '支付宝应用id',
  PRIMARY KEY (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司表';

--;; 小程序应用表
CREATE TABLE `t_app` (
	`app_id` varchar(40) NOT NULL COMMENT '主键',
	`company_id` varchar(40) NOT NULL COMMENT '所属公司',
	`app_name` varchar(40) NOT NULL COMMENT 'app名称',
	`wx_app_id` varchar(40) NOT NULL COMMENT '小程序id',
	`wx_app_secret` varchar(40) NOT NULL COMMENT '小程序密钥',
	`wx_mch_id` varchar(40) DEFAULT NULL COMMENT '小程序商户id',
	`wx_api_secret` varchar(40) DEFAULT NULL COMMENT '小程序支付API密钥',
	`wx_cert_path` varchar(100) DEFAULT NULL COMMENT '小程序支付证书路径',
	`wx_cert_pwd` varchar(20) DEFAULT NULL COMMENT '小程序支付证书密码',
  `wx_pay_body` varchar(100) DEFAULT NULL COMMENT '微信支付描述',
  `wx_notify_url` varchar(200) DEFAULT NULL COMMENT '小程序支付回调url',
	`ai_app_id` varchar(40) DEFAULT NULL COMMENT '智量id',
	`ai_app_secret` varchar(40) DEFAULT NULL COMMENT '智量secret',
  `qiniu_ak` varchar(40) DEFAULT NULL COMMENT '七牛accessToken',
  `qiniu_sk` varchar(40) DEFAULT NULL COMMENT '七牛secretToken',
  `qiniu_base_url` varchar(100) DEFAULT NULL COMMENT '七牛基础url地址',
  `qiniu_bucket` varchar(80) DEFAULT NULL COMMENT '七牛BUCKET',
	`remark` varchar(400) DEFAULT NULL COMMENT '备注',
	`delete_flag` varchar(4) DEFAULT '0' COMMENT '删除标记 0：未删除 1：已删除',
	`create_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`create_user_id` varchar(40) DEFAULT NULL COMMENT '创建人id',
  `update_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `update_user_id` varchar(40) DEFAULT NULL COMMENT '最后修改人id',
  PRIMARY KEY (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小程序应用表';

--;; company 数据
INSERT INTO `t_company` VALUES ('1', '红创管理后台', '红创管理后台系统', '1', 'http://cdn.imgs.3vyd.com/template/pc/3vyd.png', NULL, 'http://cdn.imgs.3vyd.com/template/pc/3vyd.png', NULL, NULL, '0', '2019-11-25 00:00:00', NULL, '2019-11-25 00:00:00', NULL, NULL, NULL, NULL);

--;; app数据
INSERT INTO `t_app` VALUES ('1', '1', '红创管理后台', 'wx069c4d7574501e36', 'dd83fee0e82ebf4d7218f115d49ce5ed', '1549627571', '27B937B5FB4541EA026826EDF0A5641D', NULL, 'Hczt@001', '模板template', 'https://wxqrcode.3vyd.com/api/callback', 'cshc8539693774548274', 'KIiWvPtV6Tpup9J6NGuXOcnvG4F8zpGX', 'USVsxU0jt3REMPZFg5BwYO_lHWfI-SWt5UEfaaQt', 'U6UomjXwBerfBGadBuArCoY8jkVe7woC8GvBQ6Ig', 'https://hcxcx.shcaijiang.com', 'applet', '模板小程序', '0', '2019-11-25 00:00:00', NULL, '2019-11-25 00:00:00', NULL);
