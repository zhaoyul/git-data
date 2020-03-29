(ns git-stats.common.request
  (:require
   ["antd" :as ant]
   [ajax.core :as http]
   [re-frame.core :as rf]
   [kee-frame.core :as kf]
   [git-stats.url :refer [auth-url]]
   [git-stats.common.storage :as storage]))

(defn- get-token []
  (let [token (storage/get-token-storage)]
    (if token (:access-token token) "")))

(defn- get-refresh-token []
  (let [token (storage/get-token-storage)]
    (if token (:refresh-token token) "")))

(defn- http-error [msg desc]
  (prn "请求错误" msg " " desc)
  (let [message (str "请求错误 " msg)]
    (.error ant/notification (clj->js {:message message :description desc}))))

(defn- biz-error [msg]
  (.error ant/message msg))

(defn- xhrio [method uri params request-evnet]
  {:method method
   :uri uri
   :params params
   :headers {:authorization (get-token)}
   :timeout 10000
   :format          (http/json-request-format)
   :response-format (http/json-response-format {:keywords? true})
   :on-failure [::error request-evnet]})

(kf/reg-chain
  :request
  (fn [coeffect [{:keys [method url params callback-event with-code?]}]]
    (let [request-event (:re-frame.std-interceptors/untrimmed-event coeffect)]
      {:http-xhrio (xhrio method url (or params {}) request-event)}))
  (fn [_ [{:keys [params callback-event with-code?]} data]]
    (if with-code?
      {:dispatch [callback-event data params]}
      (if (zero? (:code data))
        {:dispatch [callback-event (:data data) params]}
        (biz-error (:msg data))))))

(kf/reg-event-fx
  :request/get
  (fn [_ [data]]
    {:dispatch [:request (assoc data :method :get)]}))

(kf/reg-event-fx
  :request/post
  (fn [_ [data]]
    {:dispatch [:request (assoc data :method :post)]}))

(kf/reg-event-fx
  :request/put
  (fn [_ [data]]
    {:dispatch [:request (assoc data :method :put)]}))

(kf/reg-event-fx
  :request/delete
  (fn [_ [data]]
    {:dispatch [:request (assoc data :method :delete)]}))

(kf/reg-chain
  ::refresh-token
  (fn [_ [request-event]]
    {:http-xhrio {:method :post
                  :uri (:refresh-token auth-url)
                  :params {:refresh-token (get-refresh-token)}
                  :headers {:authorization (get-token)}
                  :timeout 10000
                  :format          (http/json-request-format)
                  :response-format (http/json-response-format {:keywords? true})
                  :on-failure [::refresh-token-error]}})
  (fn [_ [request-event {:keys [code msg data]}]]
    (if (zero? code)
      (do
        (storage/set-token-storage data)
        (rf/dispatch request-event))
      (do
        (prn "refresh token 无效，重新登录")
        (rf/dispatch [:core/nav :login])))))

(kf/reg-event-fx
  ::refresh-token-error
  (fn [_ [response]]
    (let [status (get response :status)]
      (http-error status (:refresh-token auth-url)))))


(kf/reg-event-fx
  ::error
  (fn [_ [event response]]
    (let [status (get response :status)
          url (if event (get (second event) :url "") "")]
      (if (and (= status 401) (not= (get-refresh-token) ""))
        (rf/dispatch [::refresh-token event])
        (http-error status url)))))
