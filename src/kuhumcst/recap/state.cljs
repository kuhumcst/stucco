(ns kuhumcst.recap.state
  "State for recap components is designed to work with reagent state containers,
  i.e. ratoms as well as cursors and reactions.

  Other types of state can also be used on an adhoc basis by defining custom
  `deref`, `reset!`, and `swap!` functions for that piece of state while
  ensuring that the component is re-rendered when mutating the state."
  (:require [reagent.ratom :as ratom])
  (:refer-clojure :exclude [deref reset! swap!]))


(defn- state-type
  [o & _]
  (cond
    ;; Covers RAtom, RCursor, Reaction, but not Track (no reset! or swap!).
    (and (satisfies? ratom/IReactiveAtom o)
         (satisfies? IDeref o)
         (satisfies? IReset o)
         (satisfies? ISwap o)) :ratom

    (and (map? o)
         (every? o #{:deref :reset! :swap!})) :map

    (when-let [m (meta o)]
      (every? m #{:deref :reset! :swap!})) :meta))

(defmulti deref state-type)
(defmulti reset! state-type)
(defmulti swap! state-type)

(defmethod deref :ratom [o] (clojure.core/deref o))
(defmethod reset! :ratom [a new-value] (clojure.core/reset! a new-value))
(defmethod swap! :ratom [a f & more] (apply clojure.core/swap! a f more))

(defmethod deref :map [m] ((:deref m)))
(defmethod reset! :map [m new-value] ((:reset! m) new-value))
(defmethod swap! :map [m f & more] (apply (:swap! m) f more))

(defmethod deref :meta [o] ((:deref (meta o))))
(defmethod reset! :meta [o new-value] ((:reset! (meta o)) new-value))
(defmethod swap! :meta [o f & more] (apply (:swap! (meta o)) f more))
