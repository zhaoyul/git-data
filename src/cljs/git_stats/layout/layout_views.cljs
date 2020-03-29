(ns git-stats.layout.layout-views
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   ["antd" :as ant]
   ["antd/es/locale/zh_CN" :default zhCN]
   [git-stats.layout.global-footer :refer [footer]]
   [git-stats.components.hc-img :refer [size-img]]))

(def SubMenu (.-SubMenu ant/Menu))
(def MenuItem (.-Item ant/Menu))
(def Header (.-Header ant/Layout))
(def Sider (.-Sider ant/Layout))
(def Content (.-Content ant/Layout))
(def Footer (.-Footer ant/Layout))
(def BreadcrumbItem (.-Item ant/Breadcrumb))

(def collapsed (r/atom false))

(defn bread-crumbs [routes]
  [:> ant/Breadcrumb
   [:> BreadcrumbItem "首页"]
   (when routes
     (for [{:keys [title]} routes]
       ^{:key title} [:> BreadcrumbItem title]))])

(defn dropdown-menu []
  [:> ant/Menu {:className "menu"
                :onClick (fn [menu]
                           (let [key (:key (js->clj menu :keywordize-keys true))]
                             (case key
                               "logout" (rf/dispatch [:logout]))))}
   [:> MenuItem {:key "change-password"}
    [:> ant/Icon {:type "lock"}]
    [:span "修改密码"]]
   [:> MenuItem {:key "logout"}
    [:> ant/Icon {:type "logout"}]
    [:span "退出"]]])

(defn header-dropdown []
  [:div {:className "right"}
   [:> ant/Dropdown {:overlay (r/as-element [dropdown-menu])}
    [:span {:className "action account"}
     [:> ant/Avatar {:className "avatar" :size "small" :icon "user"}]
     [:span "管理员"]]]])

(defn basic-layout [side-menus breadcrumbs switch-route]
  [:> ant/ConfigProvider {:locale zhCN}
   [:div
    [:link {:rel "stylesheet" :href "/css/layout.css"}]
    [:> ant/Layout {:style {:height "100vh"}}
     [:> Sider {:style {:z-index 10}
                :trigger nil
                :collapsible false
                :collapsed @collapsed}
      [:div {:className "logo"}
       [:> ant/Spin {:spinning @(rf/subscribe [:base/company-loading])}]
       (when-let [logo @(rf/subscribe [:base/company-logo])]
         [size-img {:src logo
                    :style {:width "2.0833vw"
                            :height "2.0833vw"
                            :margin-right "0.6944vw"}
                    :width 50
                    :height 30}])
       (when-let [name @(rf/subscribe [:base/company-name])]
         (if-not @collapsed name ""))]
      [side-menus]]
     [:> ant/Layout
      [:> Header {:style {:background "#fff" :padding 0}}
       [:> ant/Icon {:className "trigger"
                     :type (if-not @collapsed "menu-unfold" "menu-fold")
                     :onClick (fn [] (swap! collapsed not))}]
       [header-dropdown]]
      [:> ant/PageHeader {:style {:margin "1px 0" :padding "0 24px 8px 24px"}}
       [bread-crumbs breadcrumbs]]
      [:> Content {:style {:margin "10px 10px"
                           :minHeight 500}}
       [:div switch-route]]
      [:> Footer
       [footer "红创科技"]]]]]])
