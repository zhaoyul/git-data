(ns git-stats.common.utils
  (:refer-clojure :exclude [list])
  (:require [goog.object :refer [getValueByKeys]]
            [clojure.string :as s]
            [clojure.set :refer [rename-keys]]
            [clojure.walk :as w]
            [reagent.core :as r]
            ["antd" :as ant]))


(defn kebab-case->camel-case
  "kebab case to camel case, 例如: on-click to onClick"
  [input]
  (let [words (s/split input #"-")
        capitalize (->> (rest words)
                        (map #(apply str (s/upper-case (first %)) (rest %))))]
    (apply str (first words) capitalize)))


(defn map-keys->camel-case
  [data & {:keys [html-props]}]
  (let [convert-to-camel (fn [[key value]]
                           [(kebab-case->camel-case (name key)) value])]
    (w/postwalk (fn [x]
                  (if (map? x)
                    (let [new-map (if html-props
                                    (rename-keys x {:class :className :for :htmlFor})
                                    x)]
                      (into {} (map convert-to-camel new-map)))
                    x))
                data)))

(defn create-form
  "对应于 Form.create() 最主要的参数是form, from 可以是任意的hiccup, 其余的参数:

   * :options - map 对应 Form.create() 的 options. 参照:
                https://ant.design/components/form/#Form.create(options)
   * :props - props, 需要是js类型"
  [form & {:keys [options props] :or {options {} props {}}}]
  (r/create-element
   (((getValueByKeys ant "Form" "create")
     (clj->js (map-keys->camel-case options)))
    (r/reactify-component form))
   (clj->js props)))

(defn get-form
  "返回又From.create创建的 `form` 这个函数只能在form内部调用, 因为使用了reaget/current-component."
  []
  (-> (r/current-component)
      (r/props)
      (js->clj :keywordize-keys true)
      (:form)))


(defn decorate-field
  "修饰一个field, 对应于getFieldDecorator() 函数
   参数:

   * form -  `form` 对象, 获得自 `(get-form)`
   * id - field id, 支持嵌套
   * options - 用于验证field
   * field - 一般就是一个input

   参照文档:
   https://ant.design/components/form/#getFieldDecorator(id,-options)-parameters"
  ([form id field] (decorate-field form id {} field))
  ([form id options field]
   (let [field-decorator (:getFieldDecorator form)
         params (clj->js (map-keys->camel-case options))]
     ((field-decorator id params) (r/as-element field)))))


#_(defn tform []
    (fn [props]
      (let [the-form (utils/get-form)
            {:keys [getFieldDecorator
                    getFieldsError
                    getFieldError
                    isFieldTouched]} the-form
            usernameError (and (isFieldTouched "username")
                               (getFieldError "username"))]

        [:> ant/Form {:layout "inline"}
         [:> FormItem {:label "输入搜索"
                       :validateStatus (if usernameError "error" "success")
                       :help usernameError}
          (utils/decorate-field
           the-form "username" {:rules [{:required true}]}
           [:> ant/Input])]])))
