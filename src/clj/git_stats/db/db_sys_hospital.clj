(ns git-stats.db.db-sys-hospital
  (:require [conman.core :as conman]
            [git-stats.db.core :refer [*db*]]
            [hugsql-adapter-case.adapters :refer [kebab-adapter]]))

(conman/bind-connection *db* {:adapter (kebab-adapter)} "sql/sys_hospital.sql")
