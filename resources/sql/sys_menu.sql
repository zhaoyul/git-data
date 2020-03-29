-- :name insert-sys-menu! :! :n
-- :保存系统菜单
insert into t_sys_menu
(id, parent_id, company_id, name, code, path, icon, sort, create_time, delete_flag)
values
(:id, :parent-id, :company-id, :name, :code, :path, :icon, :sort, now(), 0)

-- :name upate-sys-menu! :! :n
-- :doc 更新系统菜单
update t_sys_menu set
parent_id = :parent-id,
company_id = :company-id,
name = :name,
code = :code,
path = :path,
icon = :icon,
sort = :sort
where id = :id

-- :name find-sys-menu-total :? :1
-- :doc
select count(*) as total
from t_sys_menu
where delete_flag = 0 and company_id = :company-id
--~(if (not (empty? (:name params))) (str " and name like '%" (:name params) "%'") " and 1=1")
--~(if (not (empty? (:code params))) (str " and code like '%" (:code params) "%'") " and 1=1")
--~(if (seq (:company-id params)) " and company_id = :company-id" " and 1=1")


-- :name find-sys-menus :? :*
-- :doc 查询系统菜单列表
select *
from t_sys_menu
where delete_flag = 0
--~(if (not (empty? (:name params))) (str " and name like '%" (:name params) "%'") " and 1=1")
--~(if (not (empty? (:code params))) (str " and code like '%" (:code params) "%'") " and 1=1")
--~(if (seq (:company-id params)) " and company_id = :company-id" " and 1=1")
order by create_time desc
--~(if (and (:page params) (:size params)) (str " LIMIT " (* (:page params) (:size params)) "," ":size") ";")

-- :name find-sys-menu :? :1
-- :doc 获取系统菜单
select * from t_sys_menu where delete_flag = 0 and id = :menu-id


-- :name remove-sys-menu! :! :n
-- :doc 逻辑删除系统菜单
update t_sys_menu
set delete_flag = 1
where id = :menu-id

-- :name insert-role-menus! :! :n
-- :doc 角色分配菜单
insert into t_sys_role_menu(id, role_id, menu_id)
values :tuple*:rows

-- :name remove-role-menus! :! :n
-- :doc
delete from t_sys_role_menu where role_id = :role-id

-- :name find-role-menus :? :*
-- :doc 查询角色菜单
SELECT m.* from t_sys_role r, t_sys_menu m, t_sys_role_menu rm
where rm.menu_id = m.id and r.id = rm.role_id  and r.id= :role-id
