(ns kuhumcst.recap.state
  "Specs describing the shape of all state used in recap components."
  (:require [clojure.spec.alpha :as s]))

;; TODO: or pos-int? will require reworking tabs drag/drop code slightly
(s/def ::i
  int?)

(s/def ::kv
  (s/tuple any? any?))

(s/def ::kvs
  (s/coll-of ::kv))

(s/def ::kvs+i
  (s/keys :req-un [::kvs]
          :opt-un [::i]))

(defn assert-conforms
  "Assert that the current value of `state` conforms to the given `spec`."
  [spec state]
  (when-let [error (s/explain-data spec @state)]
    (assert false (with-out-str (s/explain-out error)))))
