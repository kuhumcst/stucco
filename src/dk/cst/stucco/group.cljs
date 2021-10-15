(ns dk.cst.stucco.group
  "Reagent components for making loosely defined groupings.

  These components are used to group components together in a flexible manner,
  allowing the user to manipulate the layout directly. Ideally, these comprise
  the second (or a lower) level in a layout, with the top level composed of
  WAI-ARIA landmarks (available in the 'dk.cst.stucco.landmark' namespace).

  For more semantic groupings, take a look at the implemented WAI-ARIA patterns
  available in the 'dk.cst.stucco.pattern' namespace.

  ARIA references:
    https://www.w3.org/TR/wai-aria-practices-1.1/#aria_landmark
    https://www.w3.org/TR/wai-aria-practices-1.1/examples/landmarks/"
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [dk.cst.stucco.util.state :as state]))

(defn- redistribute
  "Redistribute `weights` such that the `delta` is subtracted from the weight at
  index `n` and added to the weight at index `m`."
  [weights m n delta]
  (assoc weights
    m (max 0 (+ (get weights m) delta))
    n (max 0 (- (get weights n) delta))))

;; TODO: less clunky css for separator (thin oval gradient?)
;; TODO: check that (count weights) matches (count vs) - in spec?
;; TODO: invisible overlay container for resize mouse handlers
(defn combination
  "A combination of `vs`, with the space optionally partitioned by `weights`.
  If no `weights` are specified, each v will initially take up equal size.
  The `vs` will typically be various functionally related Stucco components."
  [{:keys [vs weights]
    :as   state}]
  (r/with-let [state        (state/prepare ::state/vs+weights state)
               resize-state (r/atom nil)]
    (let [{:keys [vs weights]
           :or   {weights (mapv (constantly 1) (range (count vs)))}} @state
          resizing     @resize-state
          key-prefix   (hash vs)
          columns      (->> weights
                            (map #(str "minmax(min-content, " % "fr)"))
                            (interpose "var(--grid-16)")
                            (str/join " "))
          resize-begin (fn [m n]
                         (fn [e]
                           (let [elements (.. e -target -parentNode -children)
                                 widths   (for [elem (take-nth 2 elements)]
                                            (.-offsetWidth elem))]
                             (reset! resize-state {:widths (vec widths)
                                                   :m      m
                                                   :n      n
                                                   :x      (.-clientX e)}))))
          resize-move  (fn [e]
                         (when-let [{:keys [widths m n x]} @resize-state]
                           (let [x'       (.-clientX e)
                                 weights' (redistribute widths m n (- x' x))]
                             (swap! state assoc :weights weights'))))
          resize-end   #(reset! resize-state nil)]
      [:div.combination {:on-mouse-move  resize-move
                         :on-mouse-up    resize-end
                         :on-mouse-leave resize-end
                         :class          (when resizing
                                           "combination--resize")
                         :style          {:grid-template-columns columns}}
       (for [[n v] (map-indexed vector vs)
             :let [key (str key-prefix "-" (hash v) "-" n)]]
         [:<> {:key key}
          (when (> n 0)
            [:div.combination__separator
             {:class         (when (= n (:n resizing))
                               "combination__separator--resize")
              :on-mouse-down (resize-begin (dec n) n)}])
          [:div v]])])))
