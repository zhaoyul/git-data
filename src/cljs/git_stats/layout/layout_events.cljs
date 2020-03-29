(ns git-stats.layout.layout-events
  (:require
   [kee-frame.core :as kf]
   [re-frame.core :as rf]
   [git-stats.common.storage :as storage]
   [clojure.string :as string]
   [git-stats.router :refer [routes]]
   [git-stats.common.route-mapping :refer [get-routes-by-path]]))

(defn saveTokens [tokens]
  (let [tokenStr (first tokens) refreshStr (second tokens)]
    (let [token (second (string/split tokenStr "=")) refresh (second (string/split refreshStr "="))]
      (storage/set-token-storage {:access-token token :refresh-token refresh}))))

(kf/reg-controller
  :layout/breadcrumbs-controller
  {:params (fn [route] (identity route))
   :start (fn [_ route]
            (rf/dispatch [:set-breadcrumbs route]))})

(rf/reg-event-db
  :set-breadcrumbs
  (fn [db [_ {:keys [path-params]}]]
    (assoc-in
      db
      [:layout :breadcrumbs]
      (get-routes-by-path (:path path-params) @routes))))

(rf/reg-sub
  :layout/breadcrumbs
  (fn [data]
    (get-in data [:layout :breadcrumbs] [])))

(rf/reg-event-db
  :change-password
  (fn [db _]
    db))

;; 给其它系统嵌套使用
(kf/reg-controller
  ::from-other-site
  {:params (fn [route]
             (let [query (get route :query-string)]
               (when (re-find #"token=\w+" (or query ""))
                 (let [tokens (string/split query "&")]
                   (saveTokens tokens)
                   true))))
   :start [::hide-layout]})

(rf/reg-event-db
  ::hide-layout
  (fn [db]
    (assoc-in db [:layout :hide-layout] true)))

(rf/reg-sub
  :layout/hide-layout
  (fn [db]
    (get-in db [:layout :hide-layout] false)))
