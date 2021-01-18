(ns dk.cst.stucco.carousel
  "Reagent components for displaying slides in a carousel.

  State description:
    :kvs  - key-values pairs of slide labels and bodies.
    :i    - (optional) the index of the currently selected slide.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#carousel"
  (:require [reagent.core :as r]
            [dk.cst.stucco.state :as state]
            [dk.cst.stucco.dom.keyboard :as kbd]))

;; TODO: drag-and-drop

(defn carousel
  "Tabbed carousel with a slide picker, but without automatic slide rotation.
  Takes `state` of the form described in the docstring of this namespace.

  Optionally, certain HTML attributes specified in the `opts` may merged with
  the carousel attr. This should be done in order to satisfy ARIA labeling
  requirements, e.g. either :aria-label or :aria-labelledby should be set."
  [{:keys [kvs i] :as state}
   {:keys [aria-label
           aria-labelledby]
    :as   opts}]
  (r/with-let [state      (state/prepare ::state/kvs+i state)
               next-slide #(swap! state update :i inc)
               prev-slide #(swap! state update :i dec)]
    (let [{:keys [i kvs]} @state
          [label content] (nth kvs i)
          tab-panel-id (random-uuid)
          label-id     (random-uuid)
          prev?        (> i 0)
          next?        (< i (dec (count kvs)))]
      ;; This implementation most closely resembles the Tabbed Carousel:
      ;; https://www.w3.org/TR/wai-aria-practices-1.1/#tabbed-carousel-elements
      ;; The outer container follows the basic carousel pattern, while most of
      ;; the inner parts resemble a regular tabs implementation.
      [:div.carousel {:role                 "group"
                      :aria-roledescription "carousel"
                      :aria-label           aria-label
                      :aria-labelledby      aria-labelledby}
       [:button.carousel__select {:aria-label (str "View slide number " i) ;TODO: localisation
                                  :tab-index  (if prev? "0" "-1")
                                  :on-click   (when prev? prev-slide)}]
       [:div.carousel__slide {:role            "tabpanel"
                              :id              tab-panel-id
                              :aria-labelledby label-id}
        (when (> (count kvs) 2)
          [:div.carousel__slide-header
           [:div.carousel__slide-label {:id label-id} label]
           (into [:div.slide-picker {:role        "tablist"
                                     :on-key-down kbd/roving-tabindex-handler
                                     :aria-label  "Choose a slide to display"}] ;TODO: localisation
                 (for [n (range (count kvs))
                       :let [selected? (= n i)
                             select    #(swap! state assoc :i n)]]
                   [:span.slide-picker__dot {:role          "tab"
                                             :aria-controls tab-panel-id
                                             :aria-selected selected?
                                             :tab-index     (if selected?
                                                              "0"
                                                              "-1")
                                             :on-click      select}]))])
        content]
       [:button.carousel__select {:aria-label (str "View slide number " (inc i)) ;TODO: localisation
                                  :tab-index  (if next? "0" "-1")
                                  :on-click   (when next? next-slide)}]])))
