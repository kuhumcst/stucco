(ns recap.layout.core
  "Reagent components to make page layouts from top-level Landmark Regions.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#aria_landmark"
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [recap.state :as state]))

;; Some landmarks only allow single instances, e.g. banner or main.
(defn- assert-single-element
  "Assert from the `hiccup` that the `landmark-type` is a single instance."
  [landmark-type [tag & _ :as hiccup]]
  (assert (or (and (keyword? tag)
                   (not= :<> tag))
              (fn? tag))
          (str landmark-type " is not a single element: " hiccup)))

;; TODO: optional skip-link? -- https://www.youtube.com/watch?v=cOmehxAU_4s
(defn root
  "Root layout comprised of the top-level `landmarks`."
  [{:keys [banner
           complementary
           content-info
           main]
    :as   landmarks}]
  (assert-single-element "banner" banner)
  (assert-single-element "main" main)
  [:<>
   banner
   main
   complementary
   content-info])

(defn- redistribute
  "Redistribute `weights` such that the `delta` is subtracted from the weight at
  index `n` and added to the weight at index n-1."
  [weights n delta]
  (let [n-1 (dec n)]
    (assoc weights
      n-1 (max 0 (+ (get weights n-1) delta))
      n (max 0 (- (get weights n) delta)))))

;; TODO: less clunky css for separator (thin oval gradient?)
;; TODO: check that (count weights) matches (count vs) - in spec?
;; TODO: invisible overlay container for resize mouse handlers
(defn combination
  "A combination of `vs`, with the space optionally partitioned by `weights`.
  If no `weights` are specified, each v will initially take up equal size.
  The `vs` will typically be various functionally related recap components."
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
                            (interpose "var(--grid-8)")
                            (str/join " "))
          resize-begin (fn [n]
                         (fn [e]
                           (let [elements (.. e -target -parentNode -children)
                                 widths   (for [elem (take-nth 2 elements)]
                                            (.-offsetWidth elem))]
                             (reset! resize-state {:widths (vec widths)
                                                   :n      n
                                                   :x      (.-clientX e)}))))
          resize-move  (fn [e]
                         (when-let [{:keys [widths n x]} @resize-state]
                           (let [x'       (.-clientX e)
                                 delta    (- x' x)
                                 weights' (redistribute widths n delta)]
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
              :on-mouse-down (resize-begin n)}])
          [:div v]])])))
