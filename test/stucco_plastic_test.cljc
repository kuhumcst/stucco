(ns stucco-plastic-test
  (:require [clojure.test :refer [deftest testing is]]
            [dk.cst.stucco.plastic :as plastic]))

(def kvs-1
  [[:a :a]
   [:b :b]
   [:c :c]])

;; TODO: still need to make these tests, saving for later
#_(deftest mk-drag-state
    (testing "internal"
      (let [actual   (plastic/mk-drag-state {:i 0})
            expected nil]
        (is (= actual expected)))))
