(ns git-stats.layout.layout-main
  (:require
   [re-frame.core :as rf]
   [git-stats.router :refer [routes]]
   [git-stats.common.route-mapping :refer [main-switch-route]]
   [git-stats.layout.side-menu :refer [create-side-menus-by-data]]
   [git-stats.layout.layout-events :as layout-events]
   [git-stats.layout.layout-views :refer [basic-layout]]))

(def breadcrumbs (rf/subscribe [:layout/breadcrumbs]))
(def menus (rf/subscribe [:base/current-menus]))


(defn layout-page []
  (fn []
    (let [side-menus (create-side-menus-by-data @menus)
          switch-route (main-switch-route @routes)]
      [:div
       [basic-layout side-menus @breadcrumbs switch-route]])))
