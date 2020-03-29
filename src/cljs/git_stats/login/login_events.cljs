(ns git-stats.login.login-events
  (:require
    [kee-frame.core :as kf]
    [re-frame.core :as rf]
    [git-stats.url :refer [auth-url base-url]]
    [git-stats.common.storage :as storage]))


(kf/reg-controller
  :login-interceptor
  {:params (fn [route]
             (when (nil? (storage/get-token-storage))
               route))
   :start  (fn [] [:core/nav :login])})

(kf/reg-event-fx
  :login
  (fn [{:keys [db]} [params]]
    {:db       (assoc-in db [:login :loading] true)
     :dispatch [:request/post {:url            (:token auth-url)
                               :params         params
                               :with-code?     true
                               :callback-event :login/handler}]}))

(kf/reg-chain
  :login/handler
  (fn [{:keys [db]} [{:keys [code msg data]}]]
    (when (zero? code)
      (do
        (storage/set-token-storage data)
        (rf/dispatch [:base/fetch-company])
        (rf/dispatch [:core/nav :main {:path ""}])))
    {:db (update db :login #(assoc % :result true :loading false))}))

(kf/reg-event-fx
  :logout
  (fn [{:keys [db]}]
    (storage/remove-token-storage)
    {:db       db
     :dispatch [:core/nav :login]}))

(rf/reg-sub
  :login/result
  (fn [db]
    (get-in db [:login :result])))


(rf/reg-sub
  :login/loading
  (fn [db]
    (get-in db [:login :loading] false)))

(kf/reg-controller
  ::base-interceptor
  {:params (fn [route]
             (when (-> route :path-params :path (= "")) true))
   :start  (fn []
             (rf/dispatch [:base/fetch-company])
             (rf/dispatch [:base/fetch-user-menus]))})

(kf/reg-event-fx
  :base/fetch-company
  (fn [{:keys [db]} _]
    (when (nil? (get-in db [:base :company :data]))
      {:db       (assoc-in db [:base :company :loading] true)
       :dispatch [:request/get {:url            (:company base-url)
                                :callback-event :base/fetch-company-success}]})))

(kf/reg-event-fx
  :base/fetch-company-success
  (fn [{:keys [db]} [data]]
    {:db (update-in db [:base :company] #(assoc % :loading false :data data))}))

(kf/reg-event-fx
  :base/fetch-user-menus
  (fn [{:keys [db]} _]
    (when-not (get-in db [:base :menu :data])
      {:db       (assoc-in db [:base :menu :loading] true)
       :dispatch [:request/get {:url            (:current-menus base-url)
                                :callback-event :base/fetch-user-menus-success}]})))
(kf/reg-event-fx
  :base/fetch-user-menus-success
  (fn [{:keys [db]} [data]]
    {:db (update-in db [:base :menu] #(assoc % :loading false :data data))}))

(rf/reg-sub
  :base/company-loading
  (fn [db]
    (get-in db [:base :company :loading] false)))

(rf/reg-sub
  :base/company-logo
  (fn [db]
    (get-in db [:base :company :data :company-logo] nil)))

(rf/reg-sub
  :base/company-name
  (fn [db]
    (get-in db [:base :company :data :company-name] nil)))

(rf/reg-sub
  :base/company-index
  (fn [db]
    (get-in db [:base :company :data :company-index-url] nil)))

(rf/reg-sub
  :base/current-menus
  (fn [db]
    (get-in db [:base :menu])))
