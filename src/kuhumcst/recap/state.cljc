(ns kuhumcst.recap.state
  "Specs describing the shape of all state used in recap components."
  (:require [clojure.spec.alpha :as s]
            [reagent.core :as r]))

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

(s/def ::coll+i
  (s/keys :req-un [::coll ::i]))

(defn assert-valid
  "Assert that the current value of `state` conforms to the given `spec`."
  [state spec]
  (assert (s/valid? spec @state) (s/explain-str spec @state)))

(defn normalize
  "Make sure that `state` provided as a plain map can also be dereferenced."
  [state]
  (if (map? state)
    (r/atom state)
    state))

(defn prepare
  "Normalize and validate a piece of `state` according to `spec`."
  [spec state]
  (doto (normalize state)
    (assert-valid spec)))
