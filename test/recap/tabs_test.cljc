(ns recap.tabs-test
  (:require [clojure.test :refer [deftest testing is]]
            [recap.component.widget.tabs :as tabs]))

(def kvs-1
  [[:a :a]
   [:b :b]
   [:c :c]])

;; TODO: still need to make these tests, saving for later
#_(deftest mk-drag-state
    (testing "internal"
      (let [actual   (tabs/mk-drag-state {:i 0})
            expected nil]
        (is (= actual expected)))))
