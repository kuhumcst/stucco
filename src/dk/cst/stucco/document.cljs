(ns dk.cst.stucco.document
  "Simple reagent components meant to replace common HTML elements, providing
  useful interactive benefits and some validation.

  Unlike the more complex components located in e.g. 'dk.cst.stucco.pattern',
  the document components do not take any injected state. Rather, as they are
  meant to replace common HTML elements, they take an HTML `attr` map and (when
  applicable) contained HTML `content` as input args."
  (:require [reagent.core :as r]))

(defn- assert-alt
  "Assert from the `attr` that the image has an alt text."
  [{:keys [src alt] :as attr}]
  (assert alt
          (str src " lacks an alt text: " attr)))

;; TODO: aria tags, keyboard access
(defn illustration
  "Illustration that can be full-screened if need be. Replaces [:img]."
  [{:keys [src alt] :as attr}]
  (assert-alt attr)
  (r/with-let [fullscreen (r/atom false)]
    (let [fullscreen? @fullscreen
          toggle      #(swap! fullscreen not)
          attr*       (merge attr {:on-click toggle})]
      [:div.illustration (when fullscreen?
                           {:class    "illustration--fullscreen"
                            :on-click toggle})
       [:div.illustration__backdrop]
       [:img attr*]
       (when fullscreen?
         [:img attr*])])))
