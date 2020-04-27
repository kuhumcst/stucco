(ns kuhumcst.recap.dom.keyboard
  "Helpers for ARIA-compliant keyboard navigation.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#keyboard"
  (:require [clojure.set :as set]
            [kuhumcst.recap.dom.core :as dom]
            [kuhumcst.recap.dom.key :as key]))

;; https://javascript.info/bubbling-and-capturing
;; https://www.mutuallyhuman.com/blog/keydown-is-the-only-keyboard-event-we-need/

(defn select-fn
  "Intra-component selection handler that follows WAI-ARIA Authoring Practices.
  Should be used by any component with multiple focusable parts, e.g. tab-list.

  Will mutate the :i of the `state`. The selection order is determined by the
  order of the DOM siblings.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#kbd_general_within"
  [state]
  (let [prev   (set/union key/up key/left)
        next   (set/union key/down key/right)
        select (set/union key/spacebar key/enter)]
    (fn [e]
      (when (contains? (set/union prev next select) e.key)
        (.preventDefault e)
        (.stopPropagation e)
        (condp contains? e.key
          select (do
                   ;; Focus is both set directly and requested asynchronously.
                   ;; Which method is effective is determined by whether the
                   ;; element has to be re-rendered (async) or not (direct).
                   (dom/request-focus! e.target.id)
                   (.click e.target)
                   (.focus e.target))
          prev (when e.target.previousElementSibling
                 (.focus e.target.previousElementSibling))
          next (when e.target.nextElementSibling
                 (.focus e.target.nextElementSibling))
          :no-op)))))
