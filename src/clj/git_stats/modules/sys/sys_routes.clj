(ns git-stats.modules.sys.sys-routes
  (:require
   [clojure.spec.alpha :as s]
   [spec-tools.core :as st]
   [spec-tools.data-spec :as ds]
   [git-stats.common.result :refer [ok sorry]]
   [git-stats.modules.sys.sys-user-service :as service]
   [git-stats.modules.sys.sys-role-service :as role-service]
   [git-stats.modules.sys.sys-menu-service :as menu-service]))

(s/def ::id
  (st/spec
    {:spec string?
     :description "系统用户ID"}))

(s/def ::username
  (st/spec
    {:spec string?
     :description "用户名"}))

(s/def ::nickname
  (st/spec
    {:spec string?
     :description "昵称"}))

(s/def ::password
  (st/spec
    {:spec string?
     :description "昵称"}))

(def users-page-spec
  {:page :base/page
   :size :base/size
   (ds/opt :username) ::username
   (ds/opt :nickname) ::nickname})

(def user-save-spec
  {:username ::username
   :nickname ::nickname
   :password ::password})

(def user-update-spec
  {:id ::id
   :username ::username
   :nickname ::nickname})

(def change-password-spec
  {:id ::id
   :password ::password})

(defn sys-user-routes []
  ["/sys-user"
   {:swagger {:tags ["后台-系统用户管理"]}}

   ["/menus"
    {:get {:summary "当前用户菜单列表"
           :handler (fn [{user :current-user}]
                      (-> (service/get-current-user-menus user)
                          (ok)))}}]
   ["/page"
    {:get {:summary "系统用户分页列表"
           :parameters {:query users-page-spec}
           :handler (fn [{{query :query} :parameters
                          user :current-user}]
                      (-> (service/sys-user-page query user)
                          (ok)))}}]

   ["/save"
    {:post {:summary "保存系统用户"
            :parameters {:body user-save-spec}
            :handler (fn [{{params :body} :parameters
                           user :current-user}]
                       (if (service/reg-username? params)
                         (sorry -1 "用户名已经存在")
                         (do
                           (service/save-sys-user params user)
                           (ok))))}}]

   ["/update"
    {:post {:summary "更新系统用户"
            :parameters {:body user-update-spec}
            :handler (fn [{{params :body} :parameters
                           user :current-user}]
                       (if (service/reg-username? params)
                         (sorry -1 "用户名已经存在")
                         (do
                           (service/update-sys-user params user)
                           (ok))))}}]

   ["/change-pwd"
    {:post {:summary "修改密码"
            :parameters {:body change-password-spec}
            :handler (fn [{{params :body} :parameters
                           user :current-user}]
                       (service/update-password params user)
                       (ok))}}]

   ["/remove/:id"
    {:delete {:summary "系统用户删除"
              :parameters {:path (s/keys :req-un [::id])}
              :handler (fn [{{{:keys [id]} :path} :parameters
                             user :current-user}]
                         (service/remove-sys-user id user)
                         (ok))}}]])

(s/def ::role-id
  (st/spec
    {:spec string?
     :description "系统角色ID"}))

(def role-page-spec
  {:page :base/page
   :size :base/size
   (ds/opt :name) string?
   (ds/opt :code) string?})

(def role-save-spec
  {:name string?
   :code string?})

(def role-update-spec
  {:id string?
   :name string?
   :code string?})

(def role-assign-spec
  {:user-id string?
   :role-ids [string?]})

(defn sys-role-routes []
  ["/sys-role"
   {:swagger {:tags ["后台-系统角色管理"]}}

   ["/page"
    {:get {:summary "系统角色分页列表"
           :parameters {:query role-page-spec}
           :handler (fn [{{query :query} :parameters
                          user :current-user}]
                      (-> (role-service/sys-role-page query user)
                          (ok)))}}]

   ["/save"
    {:post {:summary "保存系统角色"
            :parameters {:body role-save-spec}
            :handler (fn [{{params :body} :parameters
                           user :current-user}]
                       (-> (role-service/save-sys-role params user)
                           (ok)))}}]

   ["/update"
    {:post {:summary "更新系统角色"
            :parameters {:body role-update-spec}
            :handler (fn [{{params :body} :parameters
                           user :current-user}]
                       (-> (role-service/update-sys-role params user)
                           (ok)))}}]

   ["/remove/:role-id"
    {:delete {:summary "系统角色删除"
              :parameters {:path (s/keys :req-un [::role-id])}
              :handler (fn [{{{:keys [role-id]} :path} :parameters
                             user :current-user}]
                         (role-service/remove-sys-role role-id user)
                         (ok))}}]
   ["/user/roles/:id"
    {:get {:summary "系统用户角色"
           :parameters {:path (s/keys :req-un [::id])}
           :handler (fn [{{{:keys [id]} :path} :parameters
                          user :current-user}]
                      (-> (role-service/get-user-roles id (:company_id user))
                          (ok)))}}]
   ["/assign/role"
    {:post {:summary "分配用户角色"
            :parameters {:body role-assign-spec}
            :handler (fn [{{{:keys [role-ids user-id]} :body} :parameters}]
                       (-> (role-service/assign-user-role user-id role-ids)
                           (ok)))}}]])



(s/def ::menu-id
  (st/spec
    {:spec string?
     :description "系统菜单ID"}))

(def menu-list-spec
  {(ds/opt :name) string?
   (ds/opt :code) string?})

(def menu-save-spec
  {:parent_id string?
   :name string?
   :code string?
   :path any?
   :icon any?
   :sort any?})

(def menu-update-spec
  (merge menu-save-spec {:id string?}))

(def menu-assign-spec
  {:role-id string?
   :menu-ids [string?]})

(defn sys-menu-routes []
  ["/sys-menu"
   {:swagger {:tags ["后台-系统菜单管理"]}}

   ["/list"
    {:get {:summary "系统菜单分页列表"
           :parameters {:query menu-list-spec}
           :handler (fn [{{query :query} :parameters
                          user :current-user}]
                      (-> (assoc query :company_id (:company_id user))
                          (menu-service/get-sys-menus)
                          (ok)))}}]

   ["/save"
    {:post {:summary "保存系统菜单"
            :parameters {:body menu-save-spec}
            :handler (fn [{{params :body} :parameters
                           user :current-user}]
                       (-> (assoc params :company_id (:company_id user))
                           (menu-service/save-sys-menu)
                           (ok)))}}]

   ["/update"
    {:post {:summary "更新系统菜单"
            :parameters {:body menu-update-spec}
            :handler (fn [{{params :body} :parameters
                           user :current-user}]
                       (-> (menu-service/update-sys-menu params user)
                           (ok)))}}]

   ["/remove/:menu-id"
    {:delete {:summary "系统菜单删除"
              :parameters {:path (s/keys :req-un [::menu-id])}
              :handler (fn [{{{:keys [menu-id]} :path} :parameters
                             user :current-user}]
                         (menu-service/remove-sys-menu menu-id user)
                         (ok))}}]
   ["/role/menus/:role-id"
    {:get {:summary "角色所属菜单"
           :parameters {:path (s/keys :req-un [::role-id])}
           :handler (fn [{{{:keys [role-id]} :path} :parameters
                          user :current-user}]
                      (-> (menu-service/get-role-menus role-id (:company_id user))
                          (ok)))}}]
   ["/assign/menu"
    {:post {:summary "角色分配菜单"
            :parameters {:body menu-assign-spec}
            :handler (fn [{{{:keys [role-id menu-ids]} :body} :parameters}]
                       (-> (menu-service/assign-role-menu role-id menu-ids)
                           (ok)))}}]])
