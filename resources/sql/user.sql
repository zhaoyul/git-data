-- :name find-user-by-openid-appid :? :1
-- :doc
select
u.*
from t_user u, t_company c, t_app a
where u.delete_flag = '0' and c.delete_flag = '0' and a.delete_flag = '0'
and u.company_id = c.company_id and u.app_id = a.app_id
and u.wx_open_id = :openid and a.wx_app_id = :appid

-- :name count-user-by-openid-appid :? :1
-- :doc
select count(1) as ucount from t_user u where u.app_id = :appid and u.wx_open_id =:openid

-- :name insert-user-openid! :! :n
insert into t_user(user_id,company_id,app_id,wx_open_id,wx_union_id,wx_session_key,member_end_date,member_level_id) values (:user-id,:company-id,:app-id,:wx-open-id,:wx-union-id,:wx-session-key,DATE_ADD(NOW(),INTERVAL 365 day),:member-level-id)

-- :name update-wx-user-info! :! :n
-- :doc
update t_user
set wx_nick_name=:wx-nick-name,wx_gender=:wx-gender,wx-country=:wx-country,wx_province=:wx-province,wx_city=:wx-city,
    wx_head_image_url=:wx-head-image-url,wx_mobile=:wx-mobile,wx_language=:wx-language,update_time=current_timestamp()
where app_id=:app-id and wx_open_id=:wx-open-id

-- :name update-wx-session-key! :! :1
-- :doc
update t_user
set wx_session_key=:wx-session-key,update_time=current_timestamp()
where app_id=:app-id and wx_open_id=:wx-open-id

-- :name find-user :? :1
-- :doc
select * from t_user
where delete_flag = '0' and  user_id = :user-id

-- :name get-user-list-by-condition :? :*
-- :doc 根据条件查询用户列表
select user_id, wx_nick_name, wx_gender, wx_country,wx_province,wx_city,wx_head_image_url,wx_mobile,wx_language,create_time,update_time
from t_user
where delete_flag='0' and company_id=:company-id and wx_nick_name is not null
--~(if (not (empty? (:wx-nick-name params))) (str " and wx_nick_name like '%" (:wx-nick-name params) "%'") " and 1=1")
--~(if (not (empty? (:wx-mobile params))) (str " and wx_mobile like '%" (:wx-mobile params) "%'") " and 1=1")
order by create_time desc
--~(if (and (:page params) (:size params)) (str " LIMIT " (* (:page params) (:size params)) "," ":size") ";")

-- :name get-user-count-by-condition :? :1
-- :doc 根据条件分页查询用户列表
select COUNT(1) as total
from t_user
where delete_flag = "0" and company_id = :company-id and wx_nick_name is not null
--~(if (not (empty? (:wx-nick-name params))) (str " and wx_nick_name like '%" (:wx-nick-name params) "%'") " and 1=1")
--~(if (not (empty? (:wx-mobile params))) (str " and wx_mobile like '%" (:wx-mobile params) "%'") " and 1=1")

--:name find-member-level-by-id :? :1
--:doc
SELECT member_level_id,code,name,up_level_min,up_level_max,down_level_val,remark,delete_flag,UNIX_TIMESTAMP(create_time)as create_time,create_user_id,UNIX_TIMESTAMP(update_time)as update_time,update_user_id,recharge_money FROM t_member_level WHERE member_level_id=:member_level_id AND delete_flag='0'

--:name find-member-level-by-code :? :1
--:doc
SELECT member_level_id,code,name,up_level_min,up_level_max,down_level_val,remark,delete_flag,UNIX_TIMESTAMP(create_time)as create_time,create_user_id,UNIX_TIMESTAMP(update_time)as update_time,update_user_id FROM t_member_level WHERE code=:code AND delete_flag='0'

--:name find-member-levels :? :*
--:doc
SELECT * FROM t_member_level WHERE  delete_flag='0'

--:name find-expire-user :? :*
--:doc "到期会员"
SELECT *  FROM t_user WHERE delete_flag='0' and DATEDIFF(member_end_date,NOW()) =0

--:name user-up-down! :! :n
--:doc
update t_user set  member_level_id=:member-level-id,member_start_date=now(),member_end_date=DATE_ADD(member_start_date,INTERVAL '365' day)  where user_id=:user-id

--:name find-order-user :? :*
--:doc "每天有消费的客户"
SELECT  DISTINCT u.* FROM t_user u,t_order o WHERE u.user_id=o.user_id AND DATEDIFF(pay_date,NOW())=0 and  u.member_level_id not in(SELECT member_level_id FROM t_member_level WHERE code='vip')

--:name find-user-level :? :1
--:doc
select u.*, l.code, l.name from t_user u, t_member_level l
where u.delete_flag = '0' and u.member_level_id = l.member_level_id and u.user_id = :user-id

-- :name update-use-amount! :! :1
-- :doc 更新用户钱包金额
update t_user
set use_amount = :use-amount
where user_id = :user-id

-- :name update-lock-use-amount! :! :1
-- :doc 更新用户钱包冻结金额
update t_user
set lock_use_amount = :lock-use-amount
where user_id = :user-id


-- :name statistics-users-every-day :? ：*
-- :doc 统计每天用户注册量
select count(1) as count, DATE_FORMAT(t.create_time,'%Y-%m-%d')  as createTime
from t_user t where t.create_time > STR_TO_DATE(:start_date,'%Y-%m-%d')
and t.company_id = :company-id and wx_nick_name is not null
group by createTime
order by createTime
