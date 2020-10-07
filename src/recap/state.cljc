(ns recap.state
  "Specs describing the shape of all state used in recap components."
  (:require [clojure.spec.alpha :as s]
            [clojure.set :as set]
            [reagent.ratom :as ratom]
            [reagent.core :as r])
  (:refer-clojure :exclude [derive]))

;;;; GENERIC

;; TODO: or pos-int? will require reworking tabs drag/drop code slightly
(s/def ::i
  int?)

(s/def ::v
  any?)

(s/def ::vs
  (s/coll-of ::v))

(s/def ::kv
  (s/tuple any? any?))

(s/def ::kvs
  (s/coll-of ::kv))


;;;; SPECIFIC

(s/def ::weight
  number?)

(s/def ::weights
  (s/coll-of ::weight))


;;;; STATE MAPS

(s/def ::kvs+i
  (s/keys :req-un [::kvs]
          :opt-un [::i]))

(s/def ::vs+weights
  (s/keys :req-un [::vs]
          :opt-un [::weights]))

(defn vec-dissoc
  [coll n]
  (vec (concat (subvec coll 0 n)
               (subvec coll (inc n)))))

(defn vec-assoc
  [coll n v]
  (let [[before after] (split-at n coll)]
    (vec (concat before [v] after))))

;; TODO: remove entirely if this remains unused
;; Important global DOM state is held in this singleton state atom. Components
;; can react directly to window content changes by deref'ing the atom or a
;; cursor into it. For instance, certain components may need to react to window
;; resizing events and can so do by deref'ing that value.
(defonce window
  (let [state (r/atom {:width js/window.innerWidth})]
    (set! js/window.onresize #(swap! state assoc :width js/window.innerWidth))
    state))

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

;; TODO: what about drag-and-drop of state when mutation is restricted??
(defn derive
  "Derive a reactive map from an existing `state` map and any changes in `m`.
  Unchanged keys are synchronized between the original and derived state maps."
  [state m]
  (let [shared-ks (set/difference (set (keys @state))
                                  (set (keys m)))]
    (ratom/make-reaction #(merge @state m)
                         :on-set (fn [_ m]
                                   (let [m* (select-keys m shared-ks)]
                                     (swap! state merge m*))))))
