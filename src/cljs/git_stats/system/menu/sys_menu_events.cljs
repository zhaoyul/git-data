(ns git-stats.system.menu.sys-menu-events
  (:require
   ["antd" :as ant]
   [kee-frame.core :as kf]
   [re-frame.core :as rf]
   [git-stats.url :refer [sys-url]]))

(defn- find-root-menus
  "查找父菜单"
  [menus]
  (sort-by :sort (filter #(= (:parent-id %) "0") menus)))

(defn- find-parent-children
  "获取子菜单"
  [parent menus]
  (sort-by :sort (filter #(= (:parent-id %) (:id parent)) menus)))

(defn- to-tree-data
  "转为树结构"
  [menus]
  (sort-by
    :sort
    (map
      (fn [m]
        (let [children (find-parent-children m menus)]
          (if (seq children)
            (assoc m :children children)
            (identity m))))
      (find-root-menus menus))))

(kf/reg-controller
  ::list-controller
  {:params (fn [route]
             (when (-> route :path-params :path (= "/system/menu")) true))
   :start [:system-menu/fetch-list nil]})

(kf/reg-event-fx
  :system-menu/fetch-list
  (fn [{:keys [db]} [query]]
    {:db (update-in db [:system-menu :menu-table]
                    #(assoc % :loading true))
     :dispatch [:request/get
                {:url (:menu-list sys-url)
                 :params query
                 :callback-event :system-menu/fetch-list-success}]}))

(kf/reg-event-fx
  :system-menu/fetch-list-success
  (fn [{:keys [db]} [data]]
    {:db (update-in
           db
           [:system-menu :menu-table]
           #(assoc % :loading false :data (to-tree-data data)))}))

(kf/reg-event-fx
  :system-menu/update
  (fn [{:keys [db]} [params]]
    (let [new-params (assoc params :parent-id (last (:parent-id params)))
          url (if (:id new-params) (:menu-update sys-url) (:menu-add sys-url))]
      {:db (assoc-in db [:system-menu :menu-form :submit-loading] true)
       :dispatch [:request/post {:url url
                                 :params new-params
                                 :callback-event :system-menu/update-success}]})))

(kf/reg-event-fx
  :system-menu/update-success
  (fn [{:keys [db]} [data]]
    (.success ant/message "操作成功")
    {:db (assoc-in db [:system-menu :menu-form] {})
     :dispatch [:system-menu/fetch-list nil]}))

(rf/reg-event-db
  :system-menu/menu-form-close
  (fn [db _]
    (assoc-in db [:system-menu :menu-form] {})))

(rf/reg-event-db
  :system-menu/menu-form-open
  (fn [db [_ data]]
    (if data
      (let [parent-ids (if (= (:parent-id data) "0") ["0"] ["0" (:parent-id data)])]
        (update-in db [:system-menu :menu-form] #(assoc % :visible true :data (assoc data :parent-ids parent-ids))))
      (update-in db [:system-menu :menu-form] #(assoc % :visible true :data {})))))

(kf/reg-event-fx
  :system-menu/remove
  (fn [_ [id]]
    {:dispatch [:request/delete
                {:url ((:menu-remove sys-url) id)
                 :callback-event :system-menu/remove-success}]}))

(kf/reg-event-fx
  :system-menu/remove-success
  (fn [{:keys [db]} _]
    (.success ant/message "删除成功")
    {:dispatch [:system-menu/fetch-list nil]}))

(rf/reg-sub
  :system-menu/menu-table
  (fn [data]
    (get-in data [:system-menu :menu-table])))

(rf/reg-sub
  :system-menu/menu-form
  (fn [data]
    (get-in data [:system-menu :menu-form] {})))

(rf/reg-sub
  :system-menu/menu-options
  (fn [data]
    (let [menus (get-in data [:system-menu :menu-table :data] [])
          parents (map (fn [m] {:value (:id m) :label (:name m)}) menus)]
      [{:value "0" :label "根菜单" :children parents}])))
