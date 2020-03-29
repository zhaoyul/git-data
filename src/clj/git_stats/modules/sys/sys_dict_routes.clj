(ns git-stats.modules.sys.sys-dict-routes
  (:require
    [git-stats.db.core :refer [*db*]]
    [git-stats.common.result :refer [ok sorry]]
    [git-stats.db.db-sys-dict :as db]
    [git-stats.common.utils :as utils]
    [clojure.data.json :as j]
    [clojure.spec.alpha :as s]
    [spec-tools.core :as st]
    [clojure.tools.logging :as log]
    [java-time :as time]
    [com.rpl.specter :as sp :refer [select transform ALL]]
    [clojure.string :as string]
    [schema.core :as sc]))

(s/def ::name string?)
(s/def ::parent-id int?)
(sc/defschema
  DictItem
  {:code       string?
   :name       string?
   :group-code string?
   :parent-id  int?})
(sc/defschema
  UpdateDictItem
  {:id         int?
   :code       string?
   :name       string?
   :group-code string?
   :type       int?
   :sort       int?
   :parent-id  int?})

(s/def ::get-list-params (s/keys :req-un [:base/page :base/size]
                                 :opt-un [::name ::parent-id]))

(defn sys-dict-routes []
  ["/sys"
   {:swagger {:tags ["后台-字典数据管理"]}}
   ["/dict/list"
    {:get    {:summary    "获取字典列表"
              :parameters {:query ::get-list-params}
              :handler    (fn [{{{:keys [page size name parent-id]} :query} :parameters}]
                            (if (nil? parent-id)
                              (ok {:total-elements
                                            (->> (db/get-dict-list {:name  name
                                                                    :count true})
                                                 (map :total-elements)
                                                 (first))
                                   :page page
                                   :size size
                                   :content (db/get-dict-list {:page (* page size)
                                                               :size size
                                                               :name name})})
                              (ok {:total-elements
                                            (->> (db/get-dict-details {:parent-id parent-id
                                                                       :name      name
                                                                       :count     true})
                                                 (map :total-elements)
                                                 (first))
                                   :page page
                                   :size size
                                   :parent-id parent-id
                                   :content (db/get-dict-details {:page      (* page size)
                                                                  :size      size
                                                                  :parent-id parent-id
                                                                  :name      name})})))}
     :delete {:summary    "删除字典数据"
              :parameters {:query {:id int?}}
              :handler    (fn [{{{:keys [id]} :query} :parameters}]
                            (ok "删除成功" (db/delete-dict-list! {:id id})))}
     :post   {:summary    "添加字典数据(列表parent-id为0，详情为父级id)"
              :parameters {:body DictItem}
              :handler    (fn [{{:keys [body]} :parameters}]
                            (if (= (:parent-id body) 0)
                              (ok (db/post-dict-list! body))
                              (ok (db/post-dict-details! (dissoc body :group-code)))))}
     :put    {:summary    "修改字典数据(type为0修改父节点，type为1修改字节点)"
              :parameters {:body UpdateDictItem}
              :handler    (fn [{{:keys [body]} :parameters}]
                            (if (= (:type body) 0)
                              (ok (db/update-dict-list! (dissoc body :type :sort :parent-id)))
                              (ok (db/update-dict-details! (dissoc body :type)))))}}]])
