(ns git-stats.common.utils-test
  (:require [git-stats.common.utils :as sut]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh with-sh-dir]]
            [clojure.test :as t]))



(def ^:private tmp-dir "/tmp/git-test/")
(def ^:private tmp-src-dir "/tmp/git-test/src")

(defn prepare-before-test
  "测试之前执行"
  []
  (.mkdir (io/file tmp-dir))
  (with-sh-dir tmp-dir
    (sh "sh" "-c" "echo 'hello' > abc.clj")
    (sh "git" "init")
    (sh "git" "add" "--all")
    (sh "git" "commit" "-m" "commit"))
  (.mkdir (io/file tmp-src-dir))
  (with-sh-dir tmp-src-dir
    (sh "sh" "-c" "echo 'hello' > abc.clj")
    (sh "git" "add" "--all")
    (sh "git" "commit" "-m" "commit"))
  )
(defn clean-up-after-test
  "测试之后执行"
  []
  (with-sh-dir tmp-dir
    (sh "sh" "-c" "rm -rf  /tmp/git-test/ ")    )
  )

(t/use-fixtures
  :once
  ;; :each 用于每一组deftest 都执行的场景
  ;; :once fixture在当前namespace只执行一次
  (fn [f]
    (prepare-before-test)
    (clean-up-after-test)
    ))

(sut/with-private-fns
  [git-stats.common.utils [has-sub-dir? repo-dir? file-end-with?
                           authors-stats]]
  (t/deftest 测试目录和文件相关操作
    (t/testing "第一组test"
      (t/is (nil? (has-sub-dir? (io/file tmp-dir ) ".gt")) "没有.gt文件夹")
      (t/is (has-sub-dir? (io/file tmp-dir ) ".git") "还应该有.git文件夹")
      (t/is (repo-dir? (io/file tmp-dir )))
      (t/is (nil? (repo-dir? (io/file "/" ))))

      (t/is (file-end-with? (io/file "abc.abc" ) "abc"))
      (t/is (file-end-with? (io/file "abc.abc.xf" ) "xf"))
      (t/is (not (file-end-with? (io/file "abc.abc.xf" ) "x-f")))))


  (t/deftest 测试repo相关
    (t/testing "第一组test"
      (prn  (sut/authors-stats tmp-dir))
      (t/is (let [m (sut/authors-stats tmp-dir)]
              (and (map? m)
                   (= 1 (first (vals m))))) )
      ))

  (t/deftest 测试配置文件相关
    (t/testing "第一组test"
      (prn  (sut/authors-stats tmp-dir))
      (t/is (let [m (sut/authors-stats tmp-dir)]
              (and (map? m)
                   (= 1 (first (vals m))))) )
      ))
  )
