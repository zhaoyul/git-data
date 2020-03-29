(ns git-stats.modules.sys.auth-routes
  (:require
    [spec-tools.core :as st]
    [reitit.ring.middleware.exception :as exception]
    [git-stats.common.result :refer [ok sorry]]
    [git-stats.modules.sys.auth-service :as auth]))

(def error-middleware
  (exception/create-exception-middleware
    {:sys-auth/error (fn [exception request]
                       (let [data (ex-data exception)]
                         (sorry (:key data) (:value data))))}))

(def token-spec
  {:username (st/spec
               {:spec string?
                :swagger/default "admin"
                :description "用户名"})
   :password (st/spec
               {:spec string?
                :swagger/default "admin"
                :description "密码"})})

(def refresh-token-spec
  {:refresh-token (st/spec
                    {:spec string?})})

(defn auth-routes []
  ["/auth"
   {:swagger {:tags ["后台-系统授权"]}
    :middleware [error-middleware]}

   ["/token"
    {:post {:summary "获取token"
            :parameters {:body token-spec}
            :handler (fn [{{body :body} :parameters}]
                       (-> (auth/get-token body)
                           (ok)))}}]

   ["/refresh-token"
    {:post {:summary "刷新token"
            :parameters {:body refresh-token-spec}
            :handler (fn [{{body :body} :parameters}]
                       (-> (auth/refresh-token (:refresh-token body))
                           (ok)))}}]])
