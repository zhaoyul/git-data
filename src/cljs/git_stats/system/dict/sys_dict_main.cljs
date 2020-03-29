(ns git-stats.system.dict.sys-dict-main
  (:require
    [re-frame.core :as rf]
    [git-stats.system.dict.sys-dict-events :as events]
    [git-stats.system.dict.sys-dict-sub :as sub]
    [git-stats.system.dict.sys-dict-views :as views]))

(defn sys-dict-page []
  [:div
   [views/dict-table @(rf/subscribe [:system-dict/table-list])]
   [views/dict-list-modal @(rf/subscribe [:system-dict/list-modal])]
   [views/dict-details-modal @(rf/subscribe [:system-dict/details-modal])]])
