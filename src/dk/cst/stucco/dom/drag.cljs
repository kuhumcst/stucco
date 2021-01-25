(ns dk.cst.stucco.dom.drag
  "A functional take on drag-and-drop.

  All successful drops execute the code `(drop-fn (drag-fn))` in order
  to effectuate the necessary state changes. Certain functions are merely
  required to satisfy the HTML drag-and-drop API. Such functions have been
  def'ed rather than defn'ed to mark their special status."
  (:require [dk.cst.stucco.dom.bem :as bem]))

;; TODO: safari does not display drag image, fix!

(def drag-data
  "Temporary storage for drag data.

  This circumvents the string restriction of the JavaScript dataTransfer object,
  allowing this API to store any temporary data, e.g. functions, when dragging."
  (atom {}))

(defn on-drag-start
  "The `drag-fn` is called with no args on a successful drop. The container's
  `source-id` is needed to check for a illegal drop states when dropping."
  [drag-fn source-id]
  (fn [e]
    (let [drag-id  (str (hash drag-fn))
          dt       (.-dataTransfer e)
          element  (.-target e)
          x-offset (/ (.-offsetWidth element) 2)
          y-offset (/ (.-offsetHeight element) 2)
          ghost    (.cloneNode element true)]
      ;; Store temporary data in the drag-data atom keyed to the `drag-id`.
      (swap! drag-data assoc drag-id {:drag-fn   drag-fn
                                      :source-id source-id
                                      :ghost     ghost})
      (.setData dt "drag-id" drag-id)

      ;; TODO: what about other effects, i.e. copy?
      (set! (.-effectAllowed dt) "move")
      (set! (.-dropEffect dt) "move")

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
          (bem/add-modifier! (.-parentNode element) "drag-parent")
          (bem/add-modifier! element "drag"))
        100))))

(defn on-drop
  "The `drop-fn` is called with the drag-fn's output as its input on a
  successful drop."
  [drop-fn]
  (fn [e]
    (.preventDefault e)
    (bem/remove-modifier! (.-target e) "drag-over")
    (let [target  (.-target e)
          drag-id (.getData (.-dataTransfer e) "drag-id")
          {:keys [drag-fn source-id ghost]} (get @drag-data drag-id)
          source  (js/document.getElementById source-id)]

      ;; If a drag exists, remove traces of it no matter if the drop executes.
      (when drag-fn
        (swap! drag-data dissoc drag-id)
        (.removeChild (.-parentNode ghost) ghost)

        ;; Only execute the drop when the source does not CONTAIN the dropzone!
        ;; If executed regardless, the data would vanish since the source that
        ;; contains the child that the data is being transferred to would then
        ;; be removed from the underlying collection and therefore from the DOM.
        (when (not (and source (.contains source target)))
          (drop-fn (drag-fn)))))))

;; The onDragOver handler is needed for drag-and-drop to work.
(def on-drag-over
  (fn [e]
    (.preventDefault e)
    (set! e.dataTransfer.dropEffect "move")))

;; TODO: check for illegal drop and display forbidden cursor?
(def on-drag-enter
  (fn [e]
    (.preventDefault e)
    (bem/add-modifier! (.-target e) "drag-over")))

(def on-drag-leave
  (fn [e]
    (.preventDefault e)
    (bem/remove-modifier! (.-target e) "drag-over")))

;; TODO: does not fire when the drag is internal - fix!
(def on-drag-end
  (fn [e]
    (bem/remove-modifier! (.-parentNode (.-target e)) "drag-parent")
    (bem/remove-modifier! (.-target e) "drag")))
