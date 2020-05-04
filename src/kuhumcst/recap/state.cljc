(ns kuhumcst.recap.state
  "Specs describing the shape of all state used in recap components."
  (:require [clojure.spec.alpha :as s]))

;; TODO: or pos-int? will require reworking tabs drag/drop code slightly
(s/def ::i
  int?)

;; Stricter interpretation more in line with the `coll` in `(nth coll n)` than
;; the `coll?` predicate that annoyingly accepts maps and sets too.
(s/def ::coll
  sequential?)

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
