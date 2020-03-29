(ns git-stats.modules.sys.sys-user-service
  (:require
   [clojure.string :as string]
   [git-stats.common.utils :as utils]
   [git-stats.common.encrypt :as encrypt]
   [git-stats.modules.sys.sys-user-db :as db]))

;;用户名是否已经注册
(defn reg-username? [{:keys [username id]}]
  (not (nil? (db/find-sys-user {:not-id id :username username}))))

;;保存系统用户
(defn save-sys-user [params user]
  (db/insert-sys-user!
   (merge
    params
    {:id (utils/snowflake-id)
     :create-user-id (:user-id user)
     :password (encrypt/encode (:password params))
     :company-id (:company-id user)})))

;;更新系统用户
(defn update-sys-user [{:keys [id] :as params} user]
  (when-let [db-user (db/find-sys-user {:id id})]
    (when (= (:company-id db-user) (:company-id user))
      (db/upate-sys-user!
       (merge
        params
        {:update-user-id (:user-id user)})))))

;;修改密码
(defn update-password [{:keys [id password] :as params} user]
  (when-let [db-user (db/find-sys-user {:id id})]
    (when (= (:company-id db-user) (:company-id user))
      (db/update-sys-user-password! {:id id
                                     :password (encrypt/encode password)}))))

;;删除系统用户
(defn remove-sys-user [id user]
  (when-let [db-user (db/find-sys-user {:id id})]
    (when (= (:company-id db-user) (:company-id user))
      (db/remove-sys-user! {:id id}))))

;;系统用户分页列表
(defn sys-user-page [{:keys [page size] :as query} user]
  (let [new-query (assoc query :company-id (:company-id user))]
    {:page page
     :size size
     :data (db/find-sys-users new-query)
     :total (:total (db/find-sys-user-total new-query))}))

(defn get-current-user-menus
  "获取当前用户菜单"
  [user]
  (let [menu-list (db/find-user-menus (select-keys user [:user-id]))]
    (map #(select-keys % [:id :parent-id :name :code :path :icon :sort])  menu-list)))
