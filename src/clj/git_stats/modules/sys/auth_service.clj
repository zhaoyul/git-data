(ns git-stats.modules.sys.auth-service
  (:require
   [clojure.tools.logging :as log]
   [git-stats.common.token :as token]
   [git-stats.common.encrypt :as encrypt]
   [git-stats.modules.sys.sys-user-db :as db]
   [git-stats.common.biz-error :refer [throw-error biz-error-map]]))
 
(defn get-token [{:keys [username password]}]
  (if-let [user (db/find-sys-user {:username username :password (encrypt/encode password)})]
    (token/get-token (select-keys user [:user-id :username :nickname :company-id]))
    (throw-error 1000)))

(defn refresh-token [refresh-token-str]
  (if (token/valid-refresh-token? refresh-token-str)
    (token/refresh-token refresh-token-str)
    (throw-error 1001)))
