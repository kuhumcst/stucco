(ns recap.tabs-test
  (:require [clojure.test :refer [deftest testing is]]
            [recap.tabs :as tabs]))

(def kvs-1
  [[:a :a]
   [:b :b]
   [:c :c]])

(def kvs-1
  [[:x :x]
   [:y :y]
   [:z :z]])

;; TODO: still need to make these tests, saving for later
#_(deftest mk-drag-state
    (testing "internal"
      (let [actual   (tabs/mk-drag-state {:i 0})
            expected nil]
        (is (= actual expected)))))
