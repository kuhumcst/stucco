(ns recap.widgets.document
  "Mostly dumb components for document content. Stand in for common  HTML tags."
  (:require [reagent.core :as r]))

(defn- assert-alt
  "Assert from the `attr` that the image has an alt text."
  [{:keys [src alt] :as attr}]
  (assert alt
          (str src " lacks an alt text: " attr)))

;; TODO: aria tags, keyboard access
(defn illustration
  [{:keys [src alt] :as attr}]
  (assert-alt attr)
  (r/with-let [fullscreen (r/atom false)
               toggle     #(swap! fullscreen not)]
    (let [fullscreen? @fullscreen]
      [:div.illustration {:class (when fullscreen?
                                   "illustration--fullscreen")}
       [:div.illustration__backdrop]
       [:img {:src      src
              :alt      alt
              :on-click toggle}]
       (when fullscreen?
         [:img {:src      src
                :alt      alt
                :on-click toggle}])])))