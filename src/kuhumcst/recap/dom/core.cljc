(ns kuhumcst.recap.dom.core
  "Global DOM manipulation.")

(def ^:dynamic *requested-focus*
  nil)

(defn request-focus!
  "Request that the element with the given `id` is given focus next.
  The element should have accept-focus! as its :ref handler."
  [id]
  (set! *requested-focus* id))

(defn accept-focus!
  "Focus the HTML `element` if it has been requested. Use as a :ref handler."
  [element]
  (when (and element (= (.-id element) *requested-focus*))
    (set! *requested-focus* nil)
    (.focus element)))
