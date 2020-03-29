-- :name find-app :? :1
-- :doc
select *
from t_app
where 1=1
--~(if (:app-id params) " and app_id = :app-id" " and 1=1")
--~(if (:wx-app-id params) " and wx_app_id = :wx-app-id" " and 1=1")


-- :name find-app-by-app-id :? :1
-- :doc
select * from t_app a where a.app_id = :app-id

-- :name find-app-by-company-id :? :1
-- :doc 根据company_id查询app信息
select * from t_app a where a.company_id = :company-id and a.delete_flag = 0



