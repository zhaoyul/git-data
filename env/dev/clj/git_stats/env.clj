(ns git-stats.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [git-stats.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[git-stats started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[git-stats has shut down successfully]=-"))
   :middleware wrap-dev})
