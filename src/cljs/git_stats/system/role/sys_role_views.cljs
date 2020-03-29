(ns git-stats.system.role.sys-role-views
  (:require
   ["antd" :as ant]
   ["moment" :as moment]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [git-stats.common.utils :as utils]
   [git-stats.components.common-page :as page]))


(def FormItem (.-Item ant/Form))
(def SelectOption (.-Option ant/Select))
(def TreeNode (.-TreeNode ant/Tree))
(def Password (.-Password ant/Input))

(defn- search [query]
  (rf/dispatch [:system-role/fetch-list query]))

(def columns [
              {:title "角色名" :dataIndex "name"}
              {:title "编码" :dataIndex "code"}
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
                              :event [:system-role/role-form-open data]}]
                            [:> ant/Divider {:type "vertical"}]
                            [page/remove-btn
                             {:event [:system-role/remove (:id data)]}]
                            [:> ant/Divider {:type "vertical"}]
                            [page/event-btn
                             {:title "分配菜单"
                              :event [:system-role/assign-form-open data]}]]))}])


(defn table-search []
  (utils/create-form
   (fn [props]
     (let [this (utils/get-form)]
       [:> ant/Form {:layout "inline"
                     :onSubmit (fn [e]
                                 (.preventDefault e)
                                 ((:validateFields this)
                                  (fn [_ values]
                                    (search (into {:page 0 :size 10} (remove (fn [[k v]] (nil? v)) (js->clj values)))))))}
        [:> ant/Row {:gutter 8}
         [:> ant/Col {:md 4}
          [:> FormItem
           (utils/decorate-field
            this "name"
            [:> ant/Input {:placeholder "角色名称"}])]]
         [:> ant/Col {:md 4}
          [:> FormItem
           (utils/decorate-field
            this "code"
            [:> ant/Input {:placeholder "角色编码"}])]]
         [:> ant/Col {:md 4}
          [:span {:className "submit-buttons"}
           [:> ant/Button {:type "primary" :htmlType "submit"} "查询"]
           [:> ant/Button
            {:style {:margin-left 10}
             :onClick (fn []
                         ((:resetFields this))
                         (search nil))} "重置"]]]]]))))

(defn role-table [{:keys [data loading pagination]}]
  [:div
   [page/main-page {:title "系统角色列表"
                    :event [:system-role/role-form-open]}
    [:div
     [:> ant/Row {:style {:margin-bottom 20}}
      [:> ant/Col {:span 22}
       [table-search]]]
     [page/pagination-table
      {:rowKey "id"
       :columns columns
       :loading (or loading false)
       :dataSource data
       :pagination pagination
       :onSizeChange (fn [current size] (search {:page (dec current) :size size}))
       :onChange (fn [{:keys [current pageSize] :as pagination}] (search {:page (dec current) :size pageSize}))}]]]])


(defn role-form [{:keys [visible data loading submit-loading]}]
  (utils/create-form
   (fn [props]
     (let [this (utils/get-form)
           item-col {:labelCol {:span 5} :wrapperCol {:span 15}}]
       [:> ant/Modal {:title (if (:id data) "更新系统角色" "添加系统角色")
                      :closable false
                      :visible visible
                      :confirmLoading (or submit-loading false)
                      :onCancel (fn [] (rf/dispatch [:system-role/role-form-close]))
                      :onOk (fn []
                              ((:validateFieldsAndScroll this)
                               (fn [err values]
                                 (when (not err)
                                   (let [params (js->clj values :keywordize-keys true)]
                                     (rf/dispatch [:system-role/update params]))))))}
        (utils/decorate-field this "id" {:initialValue (:id data)} [:span])
        [:> ant/Form
         [:> FormItem (merge {:label "角色名称"} item-col)
          (utils/decorate-field
           this "name"
           {:rules [{:required true
                     :message "请输入角色名"}]
            :initialValue (:name data)}
           [:> ant/Input {:placeholder "请输入角色名"}])]
         [:> FormItem (merge {:label "编码"} item-col)
          (utils/decorate-field
           this "code"
           {:rules [{:required true
                     :message "请输入编码"}]
            :initialValue (:code data)}
           [:> ant/Input {:placeholder "请输入编码"}])]]]))))

(defn assign-menu-form
  "分配菜单"
  [{:keys [role visible check-all? loading submit-loading tree checked-keys expand-keys]}]
  [:> ant/Modal {:title (str (or (:name role) "") "角色分配菜单")
                 :closable false
                 :style {:top 20}
                 :bodyStyle {:height "70vh" :overflow "auto" :padding-top 18}
                 :visible visible
                 :confirmLoading (or submit-loading false)
                 :onCancel #(rf/dispatch [:system-role/assign-form-close])
                 :onOk #(rf/dispatch [:system-role/assign-menus])}
   [:> ant/Card {:bodyStyle {:padding "5px 0px 5px 6px"} :style {:width 72}}
    [:> ant/Checkbox
     {:checked check-all?
      :onChange #(let [checked (:checked (js->clj (.-target %) :keywordize-keys true))]
                   (rf/dispatch [:system-role/on-checked-all checked]))}
     "全选"]]
   [:> ant/Spin {:spinning loading}]
   [:> ant/Tree
    {:checkable true
     :showIcon true
     :selectable false
     :checkedKeys checked-keys
     :expandedKeys expand-keys
     :onCheck #(rf/dispatch [:system-role/on-checked (js->clj %1 :keywordize-keys true)])
     :onExpand #(rf/dispatch [:system-role/on-expand (js->clj % :keywordize-keys true)])}
    (for [parent tree]
      [:> TreeNode
       {:key (:id parent)
        :icon (r/as-component [:> ant/Icon {:type (:icon parent)}])
        :title (:name parent)}
       (for [child (:children parent)]
         [:> TreeNode
          {:key (:id child)
           :title (:name child)}])])]])
