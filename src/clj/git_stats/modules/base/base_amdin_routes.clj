(ns git-stats.modules.base.base-amdin-routes
  (:require [git-stats.common.result :refer [ok sorry]]
            [git-stats.modules.base.base-db :as base-db]
            [spec-tools.data-spec :as ds]
            [reitit.ring.middleware.exception :as exception]))

(defn base-amdin-routes []
  ["/base"
   {:swagger {:tags ["后台-基础数据接口"]}}

   ["/company/detail"
    {:get {:summary "获取公司信息"
           :handler (fn [{{:keys [company-id]}:current-user}]
                      (let [company  (base-db/find-company-by-id {:company-id company-id})]
                        (if (nil? company)
                          (sorry -1 "公司信息不存在")
                          (ok company))))}}]])



