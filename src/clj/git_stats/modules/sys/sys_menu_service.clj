(ns git-stats.modules.sys.sys-menu-service
  (:require
   [clojure.string :as string]
   [conman.core :as conman]
   [git-stats.db.core :refer [*db*]]
   [git-stats.common.utils :as utils]
   [git-stats.modules.sys.sys-user-db :as db]))


(defn save-sys-menu
  "保存系统菜单"
  [params]
  (db/insert-sys-menu!
   (assoc params :id (utils/snowflake-id))))

(defn update-sys-menu
  "更新系统菜单"
  [{:keys [id] :as params} {:keys [company-id]}]
  (when-let [db-menu (db/find-sys-menu {:menu-id id})]
    (when (= (:company-id db-menu) company-id)
      (db/upate-sys-menu! (assoc params :company-id company-id)))))

(defn remove-sys-menu
  "删除系统菜单"
  [menu-id user]
  (when-let [db-menu (db/find-sys-menu {:menu-id menu-id})]
    (when (= (:company-id db-menu) (:company-id user))
      (db/remove-sys-menu! {:menu-id menu-id}))))

(defn get-sys-menus
  "所有系统菜单"
  [query]
  (db/find-sys-menus query))

(defn assign-role-menu
  "角色分配菜单"
  [role-id menu-ids]
  (conman/with-transaction [*db*]
    (db/remove-role-menus! {:role-id role-id})
    (when (seq menu-ids)
      (db/insert-role-menus!
       {:rows
        (map #(vector (utils/uuid) role-id %) menu-ids)}))))

(defn get-role-menus
  "获取角色菜单"
  [role-id company-id]
  (let [all-menus (db/find-sys-menus {:company-id company-id})
        role-menus (db/find-role-menus {:role-id role-id})]
    (map
     #(select-keys % [:id :parent-id :company-id :name :code :path :icon :sort :selected])
     (map
      (fn [menu]
        (assoc menu :selected (not (nil? (some #(= (:id menu) (:id %)) role-menus)))))
      all-menus))))
