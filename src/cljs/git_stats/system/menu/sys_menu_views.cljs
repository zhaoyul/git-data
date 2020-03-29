(ns git-stats.system.menu.sys-menu-views
  (:require
   ["antd" :as ant]
   ["moment" :as moment]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [git-stats.common.utils :as utils]
   [git-stats.components.common-page :as page]))

(def FormItem (.-Item ant/Form))

(defn- search [query]
  (rf/dispatch [:system-menu/fetch-list query]))

(def columns [
              {:title "菜单名" :dataIndex "name"}
              {:title "图标"
               :dataIndex "icon"
               :render #(r/as-element [:> ant/Icon {:type %}])}
              {:title "编码" :dataIndex "code"}
              {:title "路径" :dataIndex "path"}
              {:title "排序" :dataIndex "sort"}
              {:title "创建时间"
               :dataIndex "create-time"
               :render #(r/as-element [:span (.format (moment % (.-ISO_8601 moment)) "YYYY/MM/DD")])}
              {:title "操作"
               :align "center"
               :render #(let [data (js->clj %2 :keywordize-keys true)]
                          (r/as-element
                           [:div
                            [page/event-btn
                             {:title "编辑"
                              :event [:system-menu/menu-form-open data]}]
                            [:> ant/Divider {:type "vertical"}]
                            [page/remove-btn
                             {:event [:system-menu/remove (:id data)]}]]))}])

(defn table-search []
  (utils/create-form
   (fn [props]
     (let [this (utils/get-form)]
       [:> ant/Form {:layout "inline"
                     :onSubmit (fn [e]
                                 (.preventDefault e)
                                 ((:validateFields this)
                                  (fn [_ values]
                                    (search (into {} (remove (fn [[k v]] (nil? v)) (js->clj values)))))))}
        [:> ant/Row {:gutter 8}
         [:> ant/Col {:md 4}
          [:> FormItem
           (utils/decorate-field
            this "name"
            [:> ant/Input {:placeholder "菜单名称"}])]]
         [:> ant/Col {:md 4}
          [:> FormItem
           (utils/decorate-field
            this "code"
            [:> ant/Input {:placeholder "菜单编码"}])]]
         [:> ant/Col {:md 4}
          [:span {:className "submit-buttons"}
           [:> ant/Button {:type "primary" :htmlType "submit"} "查询"]
           [:> ant/Button
            {:style {:margin-left 10}
             :onClick (fn []
                         ((:resetFields this))
                         (search nil))} "重置"]]]]]))))

(defn menu-table [{:keys [data loading]}]
  [:div
   [page/main-page {:title "系统菜单列表"
                    :event [:system-menu/menu-form-open nil]}
    [:div
     [:> ant/Row {:style {:margin-bottom 20}}
      [:> ant/Col {:span 22}
       [table-search]]]
     [:> ant/Table
      {:rowKey "id"
       :pagination false
       :columns columns
       :loading (or loading false)
       :dataSource data}]]]])

(defn menu-form []
  (utils/create-form
   (fn [props]
     (let [this (utils/get-form)
           item-col {:labelCol {:span 5} :wrapperCol {:span 15}}
           {:keys [visible data submit-loading]} @(rf/subscribe [:system-menu/menu-form])]
       [:> ant/Modal {:title (if (:id data) "更新系统菜单" "添加系统菜单")
                      :style {:top 20}
                      :closable false
                      :visible visible
                      :confirmLoading (or submit-loading false)
                      :afterClose #((:resetFields this))
                      :onCancel (fn []
                                  (rf/dispatch [:system-menu/menu-form-close]))
                      :onOk (fn []
                              ((:validateFieldsAndScroll this)
                               (fn [err values]
                                 (when (not err)
                                   (let [params (js->clj values :keywordize-keys true)]
                                     (rf/dispatch [:system-menu/update params]))))))}
        (utils/decorate-field this "id" {:initialValue (:id data)} [:span])
        [:> ant/Form
         [:> FormItem (merge {:label "名称"} item-col)
          (utils/decorate-field
           this "name"
           {:rules [{:required true
                     :message "请输入名称"}]
            :initialValue (:name data)}
           [:> ant/Input {:placeholder "请输入名称"}])]
         [:> FormItem (merge {:label "编码"} item-col)
          (utils/decorate-field
           this "code"
           {:rules [{:required true
                     :message "请输入编码"}]
            :initialValue (:code data)}
           [:> ant/Input {:placeholder "请输入编码"}])]
         [:> FormItem (merge {:label "父菜单"} item-col)
          (utils/decorate-field
           this "parent-id"
           {:rules [{:required true
                     :message "请选择父菜单"}]
            :initialValue (:parent-ids data)}
           [:> ant/Cascader
            {:options @(rf/subscribe [:system-menu/menu-options])
             :changeOnSelect true
             :placeholder "请选择父菜单"}])]
         [:> FormItem (merge {:label "路径"} item-col)
          (utils/decorate-field
           this "path"
           {:initialValue (:path data)}
           [:> ant/Input {:placeholder "请输入路径"}])]
         [:> FormItem (merge {:label "图标"} item-col)
          (utils/decorate-field
           this "icon"
           {:initialValue (:icon data)}
           [:> ant/Input {:placeholder "请输入图标"}])]
         [:> FormItem (merge {:label "排序"} item-col)
          (utils/decorate-field
           this "sort"
           {:initialValue (:sort data)}
           [:> ant/Input {:placeholder "请输入排序"}])]]]))))
