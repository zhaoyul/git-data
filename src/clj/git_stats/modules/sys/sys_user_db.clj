(ns git-stats.modules.sys.sys-user-db
  (:require [conman.core :as conman]
            [git-stats.db.core :refer [*db*]]
            [hugsql-adapter-case.adapters :refer [kebab-adapter]]))

(conman/bind-connection *db* {:adapter (kebab-adapter)}
                        "sql/sys_user.sql"
                        "sql/sys_role.sql"
                        "sql/sys_menu.sql")
