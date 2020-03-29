(ns git-stats.system.menu.sys-menu-main
  (:require
   [re-frame.core :as rf]
   [git-stats.system.menu.sys-menu-views :as views]
   [git-stats.system.menu.sys-menu-events :as events]))

(defn sys-menu-page []
  [:div
   [views/menu-table @(rf/subscribe [:system-menu/menu-table])]
   [views/menu-form]])
