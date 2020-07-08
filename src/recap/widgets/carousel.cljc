(ns recap.widgets.carousel
  "Reagent components for displaying slides in a carousel.

  Shared state for tab components:
    :coll - a sequence of slides.
    :i    - (optional) the index of the currently selected slide.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#carousel"
  (:require [reagent.core :as r]
            [recap.state :as state]))

;; TODO: a11y, keyboard nav, docstrings
;; https://ux.stackexchange.com/questions/13951/what-is-the-difference-between-a-slider-a-gallery-and-a-carousel
;; https://www.w3schools.com/howto/howto_css_circles.asp

(defn slide-picker
  [state]
  (r/with-let [state (state/prepare ::state/coll+i state)]
    (let [{:keys [i coll]} @state]
      (into [:div.slide-picker]
            (for [n (range (count coll))
                  :let [select #(swap! state assoc :i n)]]
              [:span.slide-picker__dot {:on-click select}])))))

(defn slides
  [state]
  (r/with-let [state      (state/prepare ::state/coll+i state)
               next-slide #(swap! state update :i inc)
               prev-slide #(swap! state update :i dec)]

    (let [{:keys [i coll]} @state]
      [:div.slides
       [:div.slides__select (when (> i 0)
                              {:class    "slides__select--visible"
                               :on-click prev-slide})]
       [:div.slides__slide (nth coll i)]
       [:div.slides__select (when (< i (dec (count coll)))
                              {:class    "slides__select--visible"
                               :on-click next-slide})]])))

(defn carousel
  [state]
  (r/with-let [state (state/prepare ::state/coll+i state)]
    (let [{:keys [i coll]} @state]
      [:div.carousel
       (when (> (count coll) 2)
         [slide-picker state])
       [slides state]])))
