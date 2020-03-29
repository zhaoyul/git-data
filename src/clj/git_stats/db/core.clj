(ns git-stats.db.core
  (:require
   [clj-time.jdbc]
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as log]
   [conman.core :as conman]
   [java-time.pre-java8 :as jt]
   [git-stats.config :refer [env]]
   [mount.core :refer [defstate]]
   [hugsql-adapter-case.adapters :refer [kebab-adapter]]))

(Class/forName "net.sf.log4jdbc.DriverSpy")
(defstate ^:dynamic *db*
          :start (if-let [jdbc-url (env :database-url)]
                   (conman/connect! {:jdbc-url jdbc-url})
                   (do
                     (log/warn "database connection URL was not found, please set :database-url in your config, e.g: dev-config.edn")
                     *db*))
          :stop (conman/disconnect! *db*))

(conman/bind-connection *db*
                        "sql/queries.sql"
                        "sql/user.sql"
                        "sql/app.sql")


(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Timestamp
  (result-set-read-column [v _2 _3]
    (.toLocalDateTime v))
  java.sql.Date
  (result-set-read-column [v _2 _3]
    (.toLocalDate v))
  java.sql.Time
  (result-set-read-column [v _2 _3]
    (.toLocalTime v)))

(extend-protocol jdbc/ISQLValue
  java.util.Date
  (sql-value [v]
    (java.sql.Timestamp. (.getTime v)))
  java.time.LocalTime
  (sql-value [v]
    (jt/sql-time v))
  java.time.LocalDate
  (sql-value [v]
    (jt/sql-date v))
  java.time.LocalDateTime
  (sql-value [v]
    (jt/sql-timestamp v))
  java.time.ZonedDateTime
  (sql-value [v]
    (jt/sql-timestamp v)))

