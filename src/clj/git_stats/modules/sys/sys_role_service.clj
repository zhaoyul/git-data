(ns git-stats.modules.sys.sys-role-service
  (:require
   [clojure.string :as string]
   [conman.core :as conman]
   [git-stats.db.core :refer [*db*]]
   [git-stats.common.utils :as utils]
   [git-stats.modules.sys.sys-user-db :as db]))

(defn save-sys-role
  "保存系统角色"
  [params user]
  (db/insert-sys-role!
   (merge
    params
    {:id (utils/snowflake-id)
     :company-id (:company-id user)})))

(defn update-sys-role
  "更新系统角色"
  [{:keys [id] :as params} user]
  (when-let [db-role (db/find-sys-role {:role-id id})]
    (when (= (:company-id db-role) (:company-id user))
      (db/upate-sys-role! params))))

(defn remove-sys-role
  "删除系统角色"
  [role-id user]
  (when-let [db-role (db/find-sys-role {:role-id role-id})]
    (when (= (:company-id db-role) (:company-id user))
      (db/remove-sys-role! {:role-id role-id}))))

(defn sys-role-page
  "系统角色分页数据"
  [{:keys [page size] :as query} user]
  (let [new-query (assoc query :company-id (:company-id user))]
    {:page page
     :size size
     :data (db/find-sys-roles new-query)
     :total (:total (db/find-sys-role-total new-query))}))

(defn all-sys-roles
  "所有系统角色"
  [user]
  (db/find-sys-roles (select-keys user [:company-id])))

(defn assign-user-role
  "用户分配角色"
  [user-id role-ids]
  (conman/with-transaction [*db*]
    (db/remove-user-roles! {:user-id user-id})
    (when (seq role-ids)
      (db/insert-user-role!
       {:rows
        (map #(vector (utils/uuid) user-id %) role-ids)}))))

(defn get-user-roles
  "获取用户角色"
  [user-id company-id]
  (let [all-roles (db/find-sys-roles {:company-id company-id})
        user-roles (db/find-user-roles {:user-id user-id})]
    (prn "***" user-id user-roles)
    (map
     #(select-keys % [:id :company-id :name :code :selected])
     (map
      (fn [role]
        (assoc role :selected (not (nil? (some #(= (:id role) (:id %))  user-roles)))))
      all-roles))))
