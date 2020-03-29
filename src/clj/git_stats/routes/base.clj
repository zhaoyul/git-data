(ns git-stats.routes.base
  (:require
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.ring.coercion :as coercion]
   [reitit.coercion.spec :as spec-coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.parameters :as parameters]
   [git-stats.middleware.formats :as formats]
   [git-stats.middleware.exception :as exception]
   [git-stats.middleware.authentication :as auth]
   [git-stats.middleware.log-interceptor :refer [log-wrap]]
   [ring.util.http-response :refer :all]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [spec-tools.core :as st]))

(s/def :base/openid
  (st/spec
    {:spec string?
     :swagger/default "omoIR5aFIw6gL2YOnGjBmNk_9yrQ"
     :description "微信openid"}))

(s/def :base/appid
  (st/spec
    {:spec string?
     :swagger/default "wx069c4d7574501e36"
     :description "微信appid"}))

(s/def :base/authorization
  (st/spec
    {:spec string?
     :swagger/default
           "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjp7InVzZXItaWQiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsIm5pY2tuYW1lIjoi566h55CG5ZGYIiwiY29tcGFueS1pZCI6IjEifSwidHlwZSI6ImFjY2Vzcy10b2tlbiIsImV4cCI6MTU3NjgyNjcyNSwiaWF0IjoxNTc2ODI0OTI1fQ.GjSrlxnyE2YpTznxyGonqJ8XV6qgHGXh3TXs4kMbuw8"
     :description "token"}))

#_(s/def :store/authorization
  (st/spec
    {:spec string?
     :swagger/default
           "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjp7ImFjY291bnRfaWQiOiIyMzc1MTQ4NDU5NjI5NzcyODAiLCJhY2NvdW50X3JvbGUiOjAsInN0b3JlX25hbWUiOiLmtYvor5Xpl6jlupcy5L-u5pS5IiwidXNlcl9ubyI6IkEwMDMiLCJzdG9yZV9jb2RlIjoiVEVTVDAwMiIsImNvbXBhbnlfbmFtZSI6IuijgeWco-WVhuWfjiIsImNvbXBhbnlfaWQiOiIxIiwidXNlcl9uYW1lIjoidGVzdCIsInN0b3JlX2lkIjoiMjM3MjYxMzIxMzQ1Nzc3NjY0In0sInR5cGUiOiJhY2Nlc3MtdG9rZW4iLCJleHAiOjc1NzI1OTU5NTIsImlhdCI6MTU3MjU5NTk1Mn0.gspQso4RSqgw0xgz2srqv7YuapFJdySSy-0Ee9u2e_I"
     :description "token"}))

(s/def :base/page
  (st/spec
    {:spec            int?
     :description     "页码，从0开始"
     :swagger/default "0"
     :reason          "页码参数不能为空"}))

(s/def :base/size
  (st/spec
    {:spec            int?
     :description     "每页条数"
     :swagger/default "10"
     :reason          "条数参数不能为空"}))

(s/def :base/id
  (st/spec
    {:spec        string?
     :description "主键id"
     :reason      "主键id不能为空"}))

(defn service-routes []
  [""
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [ ;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware
                 ;; log
                 log-wrap]}])

(defn swagger-routes []
  [""
   {:no-doc true
    :swagger {:info {:title "my-api"
                     :description ""}}}
   ["/" {:get
         {:handler (constantly {:status 301 :headers {"Location" "/api-docs/index.html"}})}}]

   ["/swagger.json"
    {:get (swagger/create-swagger-handler)}]

   ["/api-docs/*"
    {:get (swagger-ui/create-swagger-ui-handler
            {:url "/swagger.json"
             :config {:validator-url nil}})}]])

(defn api-routes []
  ["/api"
   {:parameters {:header (s/keys :req-un [:base/openid :base/appid])}}])

(defn api-public-routes []
  ["/api/public"
   {:parameters {:header (s/keys :req-un [:base/appid])}}])

(defn api-callback-routes []
  ["/api/callback"])

(defn admin-routes []
  ["/admin"
   {:parameters {:header (s/keys :req-un [:base/authorization])}
    :middleware [auth/auth-token-wrap]}])

(defn admin-public-routes []
  ["/admin/public"
   ])
