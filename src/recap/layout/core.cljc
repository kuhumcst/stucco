(ns recap.layout.core
  "Reagent components to make page layouts from top-level Landmark Regions.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#aria_landmark"
  (:require [reagent.core :as r]
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

(defn- pixels
  "Calculate exact pixel widths based on `weights` and the total `width`."
  [weights width]
  (let [sum-weights    (reduce + weights)
        scaling-factor (/ width sum-weights)]
    (map (partial * scaling-factor) weights)))

;; For dealing with potentially inexact floating point comparisons.
(defn- fuzzy=
  "`x` equals `y`, but any deviation within a -1 to +1 bound is also valid."
  [x y]
  (< (dec y) x (inc y)))

(defn- mv-pixels
  [pxs n delta]
  (let [[before after] (split-at n pxs)]
    (concat (butlast before)
            [(+ (last before) delta)]
            [(- (first after) delta)]
            (rest after))))

(defonce resize
  (atom {}))

(defonce empty-img
  (let [img (js/document.createElement "img")]
    (set! (.-src img) "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==")
    img))

(defn combination
  "A combination of `vs`, with the space optionally partitioned by `weights`.
  If no `weights` are specified, each v will initially take up equal size.
  The `vs` will typically be various functionally related recap components."
  [{:keys [vs weights]
    :as   state}]
  (r/with-let [state        (state/prepare ::state/vs+weights state)
               window-width (r/cursor state/window [:width])]
    @window-width
    (let [{:keys [vs weights pxs elem]
           :or   {weights (map (constantly 1) (range (count vs)))}} @state]
      ;; The widget cannot be fully displayed until its width is known.
      ;; The width is needed for the weights to be translated into pixel values.
      ;; When the width changes, the calculated pixel values are used as weights
      ;; to calculate a new collection of pixel values.
      [:div {:ref (fn [e]
                    (when (and e (not elem))
                      (swap! state assoc :elem e)))}
       (when elem
         (let [key-prefix (hash vs)
               width      (.-offsetWidth elem)
               pxs        (if (fuzzy= (reduce + pxs) width)
                            pxs
                            (pixels (or pxs weights) (.-offsetWidth elem)))]
           ;; TODO: compute minimum-width bounds?
           ;; https://www.geeksforgeeks.org/how-to-determine-the-content-of-html-elements-overflow-or-not/
           [:div.combination {:style {:display "flex"}}
            (for [[n [v px]] (map-indexed vector (map vector vs pxs))
                  :let [key   (str key-prefix "-" (hash v) "-" n)
                        width (str "calc(" px "px - var(--grid-4))")]]
              [:<> {:key key}
               (when (> n 0)
                 [:div.combination__separator
                  {:draggable     true
                   :on-drag-start (fn [e]
                                    (let [dt (.-dataTransfer e)]
                                      (.setDragImage dt empty-img 0 0)
                                      (reset! resize {:init-x   (.-clientX e)
                                                      :init-pxs pxs})))
                   :on-drag       (fn [e]
                                    (let [{:keys [init-x init-pxs]} @resize
                                          x*    (.-clientX e)
                                          delta (- x* init-x)
                                          pxs*  (mv-pixels init-pxs n delta)]
                                      (when (and (not= x* 0)
                                                 (not= pxs* pxs))
                                        (swap! state assoc :pxs pxs*))))}])
               [:div {:style {:width width}}
                v]])]))])))
