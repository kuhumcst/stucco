(ns dk.cst.stucco.dom.focus
  "Reactive focus manipulation.")

(def ^:dynamic *requested-focus*
  nil)

(defn request!
  "Request that the element with the given `id` is given focus next.
  The element should have accept-focus! as its :ref handler."
  [id]
  (set! *requested-focus* id))

(defn accept!
  "Focus the HTML `element` if it has been requested. Use as a :ref handler."
  [element]
  (when (and element (= (.-id element) *requested-focus*))
    (set! *requested-focus* nil)
    (.focus element)))
