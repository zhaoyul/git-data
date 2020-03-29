(ns git-stats.app
  (:require [git-stats.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init! false)
