(ns dk.cst.stucco.util.state
  "Specs describing the shape of all state used in Stucco components."
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

(defn- mk-drag-state
  [{:keys [kvs i] :or {i 0}} n]
  {:kvs (vec-dissoc kvs n)
   :i   (cond
          (= n i) (min i (- (count kvs) 2))                 ; go right
          (< n i) (dec i)                                   ; go left
          (> n i) i)})                                      ; stay in place

(defn- mk-drop-state
  [{:keys [kvs i] :or {i 0}} n kv]
  {:kvs (vec-assoc kvs n kv)
   :i   (cond
          (:selected? (meta kv)) n                          ; go to dropped kv
          (= n i) (inc i)                                   ; go right
          (< n i) (inc i)                                   ; go right
          (> n i) (max 0 i))})                              ; stay in place

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
  "Derive a reactive map from existing `parent` state and any changes in `m`.

  The keys found in the `parent` but not in `m` are synchronized between the
  parent and the derived reaction. This effectively results in a two-way data
  binding for these shared keys."
  [parent m]
  (let [parent-state @parent
        shared-ks    (set/difference (set (keys parent-state))
                                     (set (keys m)))
        child        (atom (merge parent-state (apply dissoc m shared-ks)))]
    (ratom/make-reaction
      ;; The derived state updates when the parent changes, but ignores all
      ;; changes not present in the keys shared with the child.
      #(merge @parent (apply dissoc @child shared-ks))

      ;; Child state updates are applied to the parent, but only shared keys!
      :on-set (fn [_ newstate]
                (reset! child newstate)
                (swap! parent merge (select-keys newstate shared-ks)))

      :auto-run true)))

(defn ghost
  "Create a ghost of existing `src` state and a `kmap` of keys to be renamed.
  Optionally: provide a `path` to use a cursor rather than the `src` itself.

  The intention of this function is to allow two separate components to share
  source state while focusing on different aspects of it, e.g. the components
  might want to share the :i key, while having separate values for :kvs."
  ([src kmap]
   (ghost src nil kmap))
  ([src path kmap]
   (let [state      (if path (ratom/cursor src path) src)
         small-kmap (remove (partial apply =) kmap)         ; optimization
         inv-kmap   (set/map-invert small-kmap)]
     (ratom/make-reaction
       #(-> (deref state)
            (select-keys (keys kmap))
            (set/rename-keys small-kmap))
       :on-set #(apply swap! state merge (set/rename-keys %2 inv-kmap))
       :auto-run true))))
