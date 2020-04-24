(ns kuhumcst.recap.drag
  "A functional take on drag-and-drop.

  All successful drops execute the code `(drop-fn (drag-fn))` in order
  to effectuate the necessary state changes."
  (:require [clojure.string :as str]))

(def bem-block
  #"(\w-?)+")

;; Assumes BEM convention is respected, i.e. only a single block class applied.
(defn- add-modifier
  [element modifier]
  (let [class-list (.-classList element)
        class      (str (->> (array-seq class-list)
                             (filter (partial re-matches bem-block))
                             (first))
                        "--" modifier)]
    (.add class-list class)))

(defn- remove-modifier
  [element modifier]
  (let [class-list (.-classList element)
        class      (->> (array-seq class-list)
                        (filter #(str/ends-with? % (str "--" modifier)))
                        (first))]
    (.remove class-list class)))

(def drag-fns
  "Temporary storage for drag-fns."
  (atom {}))

(defn on-drag-start
  "The `drag-fn` is called with no args on a successful drop."
  [drag-fn]
  (fn [e]
    (let [drag-id (str (hash drag-fn))]
      (swap! drag-fns assoc drag-id drag-fn)
      (set! e.dataTransfer.effectAllowed "move")
      (set! (.-dropEffect (.-dataTransfer e)) "move")
      (.setData (.-dataTransfer e) "fn" drag-id))))

;; The onDragOver handler is needed for drag-and-drop to work.
(defn on-drag-over
  []
  (fn [e]
    (.preventDefault e)
    (set! e.dataTransfer.dropEffect "move")))

(defn on-drag-enter
  []
  (fn [e]
    (.preventDefault e)
    (add-modifier (.-target e) "drag-over")))

(defn on-drag-leave
  []
  (fn [e]
    (.preventDefault e)
    (remove-modifier (.-target e) "drag-over")))

(defn on-drop
  "The `drop-fn` is called with the drag-fn's output as its input on a
  successful drop."
  [drop-fn]
  (fn [e]
    (.preventDefault e)
    (remove-modifier (.-target e) "drag-over")
    (let [drag-id (.getData (.-dataTransfer e) "fn")
          drag-fn (get @drag-fns drag-id)]
      (when drag-fn
        (swap! drag-fns dissoc drag-id)
        (drop-fn (drag-fn))))))
