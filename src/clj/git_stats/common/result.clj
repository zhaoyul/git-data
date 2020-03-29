(ns git-stats.common.result)

;;ok code 约定0
(def ok-code 0)

;;ok msg 约定
(def ok-msg "操作成功")

(defn ok-body
 ([]
  {:code    ok-code
   :message ok-msg})

 ([data]
  {:code    ok-code
   :message ok-msg
   :data    data})

 ([message data]
  {:code    ok-code
   :message message
   :data    data}))


(defn ok
  "返回一个api期待的map
  结构形如：{:status 200
           :body {:code 1
                  :message '操作成功'
                  :data map or vector}}"
  ([]
   {:status 200
    :body   (ok-body)})

  ([data]
   {:status 200
    :body   (ok-body data)})

  ([message data]
   {:status 200
    :body   (ok-body message data)}))


;;sorry msg 约定
(def sorry-msg "操作失败")

(defn sorry-body
 ([code]
  {:code    code
   :message sorry-msg})

 ([code message]
  {:code    code
   :message message}))

(defn sorry
  "返回一个catch的map
  结构形如：{:status 400
           :body {:code 10020  ;业务代码
                  :message '操作失败'}}"
  ([code]
   {:status 400
    :body   (sorry-body code)})

  ([code message]
   {:status 400
    :body   (sorry-body code message)}))

(defn unauthorized
  ([message]
   {:status 401
    :body (sorry-body "unauthorized" message)}))
