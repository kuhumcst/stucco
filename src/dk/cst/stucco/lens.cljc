(ns dk.cst.stucco.lens
  "Reagent components for inspecting generic data."
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [clojure.pprint :refer [pprint]]
            [dk.cst.stucco.dom.drag :as drag]))

;; TODO: incorrect for any combination of ns and symbol containing $
(defn- f->symbol
  "Approximate the symbol of function `f` from its .-name property."
  [f]
  (let [parts (str/split (.-name f) #"\$")]
    (-> (str/join (interpose "." (butlast parts)))
        (str "/" (last parts))
        (symbol))))

(defn- coll->code
  [coll]
  (walk/postwalk (fn [x]
                   (if (fn? x)
                     (f->symbol x)
                     x))
                 coll))

;; TODO: experiment - can this be an editable text field?
;; TODO: make appendable state, 2-column table view
;; TODO: make default operation copy rather than move
;; TODO: make accessible
(defn code
  "Drop-zone for inspecting data as code. Accepts `state` as optional param."
  [state]
  (let [delete (fn []
                 (when-let [*state @state]
                   (reset! state nil)
                   *state))
        insert (fn [x] (reset! state x))]

    (fn []
      (let [*state @state]
        [:pre {:on-drag-over (drag/on-drag-over)
               :on-drop      (drag/on-drop insert)}
         (if (empty? *state)
           [:code.code-lens.code-lens--empty "( )"]
           [:code.code-lens {:draggable     true
                             :on-drag-start (drag/on-drag-start delete)}
            (when-let [metadata (meta *state)]
              [:div.code-lens__meta {:title "Metadata"}
               "^" (with-out-str (pprint (coll->code metadata)))])
            (when *state
              (with-out-str (pprint (coll->code *state))))])]))))
