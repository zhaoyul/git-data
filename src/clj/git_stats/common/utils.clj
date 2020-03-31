(ns git-stats.common.utils
  (:require
   [clojure.string :as s]
   [clojure.edn :as edn]
   [java-time :as jt]
   [clojure.java.io :as io]
   [clj-jgit.porcelain :refer [git-blame load-repo git-log git-fetch git-clone git-add git-commit git-push]]
   [clj-jgit.querying :refer [rev-list commit-info changed-files-with-patch]])
  (:import (com.twitter.snowflake.sequence SnowFlakeGenerator)
           (java.util.concurrent TimeUnit)))


;;; git utils

(def db (atom {}))

(def disk "stats.edn")

(defn- db->disk []
  (spit disk @db))

(defn- disk->db []
  (reset! db (edn/read-string (slurp disk))))

(defn init-db []
  (disk->db))

(defn j-u-d [jt]
  (java.util.Date/from jt))


(comment
  (db->disk)
  (disk->db)

  )


(jt/minus (jt/local-date) (jt/days 1))



(defn- has-sub-dir?
  "判断dir下是否有名为 sub-dir-name 的子文件夹"
  [dir sub-dir-name]
  (let [files (vec (.listFiles dir))]
    (->> files
         (filter (fn [f] (and (.isDirectory f)
                             (= sub-dir-name (.getName f)))))
         seq)))

(defn- repo-dir?
  "判断当前文件夹是否是个git代码库"
  [dir]
  (and (.isDirectory dir)
       (has-sub-dir? dir ".git")))

(defn- src-dir?
  "判断当前src目录是不是代码目录, 主要用来排除node_modules的src目录"
  [dir]
  (and (.isDirectory dir)
       (= (.getName dir) "src")
       (not (s/includes? (.getParent dir) "node_modules"))))


(defn file-end-with?
  "文件是否以某后缀结束"
  [f suffix]
  (s/ends-with? (.getName f) suffix))

(def src-suffix-set #{"clj" "cljs" "js" "css" "sql" "vue"})

(defn- file-suffix
  "返回文件的后缀名"
  [file-name]
  (s/replace file-name #".*\." "") )

(defn- is-src-file?
  "是否为源代码"
  [f]
  (src-suffix-set (file-suffix (.getName f))))

(defn- repo-root
  "获得repo相对于当前执行目录的位置, 为库调用做准备"
  [file]
  (loop [parent (.getParent file)]
    (if (repo-dir? (io/file parent))
      parent
      (recur (.getParent (io/file parent))))))

(defn- file-path-in-repo
  "处理代码文件的名称:
   ../customplatform/store-pc/src/cljs/store_pc/quick_custom/information_view.cljs
  处理为:
  store-pc/src/cljs/store_pc/quick_custom/information_view.cljs"
  [file-name]
  (let [repo-root-path (repo-root (io/file file-name))]
    (-> file-name
        (s/replace repo-root-path "")
        (s/replace-first "/" "" ))))


(defn- src-files-lst
  "从src目录下返回该目录的所有代码文件"
  [src-dir]
  (->> src-dir
       file-seq
       (filter (fn [f]
                 (and (.isFile f)
                      (is-src-file? f))))))



(defn- file-full-name
  "拼接文件的全名"
  [io-file]
  (str (.getParent io-file) "/" (.getName io-file)))

(defn- all-commits [repo]
  (->>  (rev-list repo)
        (map  (partial commit-info repo))))


(defn- time-slices
  "可以接受的参数 :m :w :d"
  [time-unit start-time end-time]
  )



(defn src-dirs
  "返回当前代码文件夹下所有的src目录"
  [dir]
  (let [all-files (file-seq dir)
        dirs (->> all-files
                  (filter src-dir?))]
    dirs))

(defn blame-file
  "使用blame处理代码, 得到每个人在该文件的行数"
  [repo file-name]
  (->> file-name
       file-path-in-repo
       (git-blame repo)
       (map (fn [m] {:name (get-in m [:author :name])
                    :file (file-suffix (get-in m [:source-path]))}))
       (group-by identity)
       (reduce-kv (fn [m k v]
                    (assoc m k (count v)))
                  {})))


(defn authors-stats [repo-dir-path]
  (let [repo (load-repo repo-dir-path)]
    (->> repo-dir-path
         io/file
         src-dirs
         (mapcat src-files-lst)
         (map file-full-name)
         (map (partial blame-file repo))
         (reduce  (fn [r m]
                    (merge-with + r m))
                  {}))))


(comment

  (->> "../customplatform"
       authors-stats)

  (defonce blog (load-repo "../blog"))
  (->>  (rev-list blog)
        (map  (partial commit-info blog)))

  (->> "../customplatform"
       io/file
       src-dirs
       first
       src-files-lst
       (map file-full-name)
       (map blame-file)
       (reduce  (fn [r m]
                  (merge-with + r m))
                {})
       )

  (clojure.pprint/pprint (->> (src-files-lst  (first (src-files (io/file "../customplatform")) ))
                              (map file-full-name)))

  (count (file-seq  (io/file "../customplatform")))

  (def test-repo (load-repo "../customplatform"))

  (git-log test-repo :max-count 1)


  (->> (git-blame test-repo "README.md")
       (map :author)
       (map :name)
       (group-by identity)
       (reduce-kv (fn [m k v]
                    (assoc m k (count v)))
                  {})
       )


  (blame-file "../customplatform/README.md")
  
  (def repo-dir (io/file "../customplatform"))
  (repo-dir? (io/file "../customplatform"))
  (has-sub-dir? repo-dir ".git")
  (clojure.pprint/pprint (src-files (io/file "../customplatform")))
  )
;; => nil
;; => nil





(defmacro with-private-fns
  "Refers private fns from ns and runs tests in context."
  [[ns fns] & tests]
  `(let ~(reduce #(conj %1 %2 `(ns-resolve '~ns '~%2)) [] fns)
     ~@tests))

(def year-month-day
  "yyyy-MM-dd")

(def year-month-day-hour-minute-second
  "yyyy-MM-dd HH:mm:ss")

(def year-month-day-hour-minute-second-no-line
  "yyyyMMddHHmmss")

(defn generate-db-id []
  (s/replace (str (java.util.UUID/randomUUID)) "-" ""))

(defn format-date [timestamp]
  (-> year-month-day
      (java.text.SimpleDateFormat.)
      (.format timestamp)))

(defn format-time [timestamp]
  (-> year-month-day-hour-minute-second
      (java.text.SimpleDateFormat.)
      (.format timestamp)))

(defn format-date-time [timestamp]
  (-> year-month-day-hour-minute-second-no-line
      (java.text.SimpleDateFormat.)
      (.format timestamp)))

(defn uuid []
  (s/replace (str (java.util.UUID/randomUUID)) "-" ""))

(def snowFlakeGenerator
  (new SnowFlakeGenerator (TimeUnit/MILLISECONDS) 41 10 12 1514736000000 20))

(defn snowflake-id 
  "生成数据库ID，数字格式，并按照从小到大顺序生成,
   (snow flake id 时间戳从2018-1-1开始)"
  [] (str (.nextId snowFlakeGenerator)))

(defn parse-int
  "string 转 int"
  [s]
  (Integer/parseInt (re-find #"\A-?\d+" s)))

(defn parse-double
  "string 转 doule"
  [s]
  (Double/parseDouble s))

(defn parse-float
  "string 转 float"
  [s]
  (Float/parseFloat s))

;;md5加密
(defn get-str-md5 [s]
  (s/join
   (map (partial format "%02x")
        (.digest (doto (java.security.MessageDigest/getInstance "MD5")
                   .reset
                   (.update (.getBytes s)))))))

(defn encode-base64 [to-encode]
  (.encode (java.util.Base64/getEncoder) (.getBytes to-encode)))

(defn encode-base64-string [to-encode]
  (.encodeToString (java.util.Base64/getEncoder) (.getBytes to-encode)))

;; base64-decode
(defn decode-base64 [to-decode]
  (String. (.decode (java.util.Base64/getDecoder) to-decode)))

(defn decode-base64-byte [to-decode]
  (.decode (java.util.Base64/getDecoder) to-decode))


(defn group-data-by-keys
  "对一组数据库返回结果{data}进行处理, 使用{group-keys}中的key进行group-by:
  (sut/group-data-by-keys test-dict
                                  [:group-code1
                                   :group-code2]
                                  )
  可以带多组额外的集合函数，多组[key reducing-function init-value]的格式：
  (sut/group-data-by-keys test-dict
                                  [:group-code]

                                  :a
                                  (fn [v e] (+ v (:id e)))
                                  0

                                  :b
                                  (fn [v e] (+ v (:id e)))
                                  0
                                  )"
  ([data group-keys]
   (->> data
        (group-by (fn [m] (select-keys m group-keys)))

        (reduce-kv (fn [m k v]
                     (assoc m k {:list
                                 (mapv
                                  (fn [e]
                                    (apply dissoc e group-keys))
                                  v)}))
                   {})
        (mapv (fn [e] (apply merge e)))))

  ([data group-keys key r-func val & krvs]
   (->> data
        (group-by (fn [m] (select-keys m group-keys)))

        (reduce-kv (fn [m k v]
                     (assoc m
                            k (if (empty? krvs)
                                {:list
                                 (mapv
                                  (fn [e]
                                    (apply dissoc e group-keys))
                                  v)
                                 key (reduce r-func val v)}

                                (apply assoc {:list
                                              (mapv
                                               (fn [e]
                                                 (apply dissoc e group-keys))
                                               v)
                                              key (reduce r-func val v)}


                                       (let [krvs-seq (partition 3 krvs)]
                                         (mapcat (fn [krv]
                                                   (let [[key r-func val] krv]
                                                     [key (reduce r-func val v)]))
                                                 krvs-seq))))))

                   {})
        (map (fn [e] (apply merge e))))))

(defn list-from-group-by
  "去除分组, 作用和group-data-by-key 相反:
  一层嵌套: (list-from-group-by {:y 1 :a [{:b 2} {:b 3}]} )))  ->  [{:b 2, :y 1} {:b 3, :y 1}]


 双层嵌套:  (mapcat sut/list-from-group-by
                  (sut/list-from-group-by {:y 1 :a [{:b [{:x 99}]} {:b [{:x 77}]}]} ))

           ->

           [{:x 99, :y 1} {:x 77, :y 1}]
 "
  [m]
  (reduce-kv (fn [r k v]
               (if (vector? v)
                 (->> (apply conj r v)
                      (map #(merge % (dissoc m k))))

                 r))

             []
             m))
