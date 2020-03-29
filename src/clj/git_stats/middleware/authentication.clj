(ns git-stats.middleware.authentication
  (:require
   [git-stats.common.token :as token]
   [git-stats.db.core :as db]))

(defn auth-token-wrap [handler]
  (fn [request]
    (let [token-str (get-in request [:headers "authorization"])]
      (if (token/valid-access-token? token-str)
        (let [user (token/get-user token-str)]
          (handler (assoc request :current-user user)))
        (throw (ex-info "token错误" {:type :auth/unauthorized}))))))

(defn- get-current-user
  [openid appid]
  (let [user (db/find-user-by-openid-appid {:openid openid :appid appid})]
    (when user
      (let [level (db/find-member-level-by-id {:member-level-id (:member-level-id user)})]
        (assoc
         (select-keys user [:user-id :wx-open-id :wx-nick-name :wx-mobile :wx-head-image-url :wx-session-key :use-amount])
         :level-code (:code level))))))

(defn- get-current-app
  [appid]
  (when-let [app (db/find-app {:wx-app-id appid})]
    (select-keys app [:app-id :wx-app-id :wx-app-secret :app-name :company-id :ai-app-id :ai-app-secret :wx-mch-id :wx-pay-body :wx-api-secret :wx-notify-url])))

(defn auth-openid-wrap [handler]
  (fn [request]
    (let [openid (get-in request [:headers "openid"])
          appid (get-in request [:headers "appid"])
          user (get-current-user openid appid)
          app (get-current-app appid)]
      (if user
        (handler (assoc request :current-user user :current-app app))
        (throw (ex-info (format "微信openid【%s】或微信appid【%s】错误" openid appid) {:type :auth/unauthorized}))))))

(defn auth-appid-wrap [handler]
  (fn [request]
    (let [appid (get-in request [:headers "appid"])
          app (get-current-app appid)]
      (if app
        (handler (assoc request :current-app app))
        (throw (ex-info (format "微信appid【%s】错误" appid) {:type :auth/unauthorized}))))))
