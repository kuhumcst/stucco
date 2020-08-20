(ns recap.dom.drag
  "A functional take on drag-and-drop.

  All successful drops execute the code `(drop-fn (drag-fn))` in order
  to effectuate the necessary state changes."
  (:require [recap.dom.interop :as interop]))

;; TODO: safari does not display drag image, fix!

(def drag-fns
  "Temporary storage for drag-fns."
  (atom {}))

(defn on-drag-start
  "The `drag-fn` is called with no args on a successful drop."
  [drag-fn]
  (fn [e]
    (let [drag-id  (str (hash drag-fn))
          dt       (.-dataTransfer e)
          element  (.-target e)
          x-offset (/ (.-offsetWidth element) 2)
          y-offset (/ (.-offsetHeight element) 2)
          ghost    (.cloneNode element true)]
      (swap! drag-fns assoc drag-id {:drag-fn drag-fn
                                     :ghost   ghost})
      ;; TODO: what about other effects, i.e. copy?
      (set! (.-effectAllowed dt) "move")
      (set! (.-dropEffect dt) "move")
      (.setData dt "fn" drag-id)

      ;; The ghost is so we can differentiate source and the drag image styling.
      (.add (.-classList ghost) "--ghost")
      (.setAttribute ghost "aria-hidden" "true")
      (js/document.body.appendChild ghost)
      (.setDragImage dt ghost x-offset y-offset)

      ;; Modifying a dragged element after an onDragStart event will glitch both
      ;; Chrome and Safari, making this slight delay necessary. Firefox is OK.
      ;; The drag-parent modifier class is also necessary to disable :hover
      ;; effects. Chrome seems to otherwise temporarily remove the DOM element,
      ;; triggering :hover on the element to the right.
      (js/setTimeout
        (fn []
          (interop/add-modifier! (.-parentNode element) "drag-parent")
          (interop/add-modifier! element "drag"))
        100))))

;; TODO: does not fire when the drag is internal - fix!
(defn on-drag-end
  []
  (fn [e]
    (interop/remove-modifier! (.-parentNode (.-target e)) "drag-parent")
    (interop/remove-modifier! (.-target e) "drag")))

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
    (interop/add-modifier! (.-target e) "drag-over")))

(defn on-drag-leave
  []
  (fn [e]
    (.preventDefault e)
    (interop/remove-modifier! (.-target e) "drag-over")))

(defn on-drop
  "The `drop-fn` is called with the drag-fn's output as its input on a
  successful drop."
  [drop-fn]
  (fn [e]
    (.preventDefault e)
    (interop/remove-modifier! (.-target e) "drag-over")
    (let [drag-id (.getData (.-dataTransfer e) "fn")
          {:keys [drag-fn ghost]} (get @drag-fns drag-id)]
      (when drag-fn
        (swap! drag-fns dissoc drag-id)
        (drop-fn (drag-fn))
        (.removeChild (.-parentNode ghost) ghost)))))
