(ns user
  (:require [clojure.pprint :refer [pprint]]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [reagent.ratom :as ratom]
            [kuhumcst.recap.lens :as lens]
            [kuhumcst.recap.layout.core :as layout]
            [kuhumcst.recap.layout.landmarks :as landmarks]
            [kuhumcst.recap.tabs :refer [tabs] :as tabs]))

(def lorem-ipsum-1
  "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod
  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,
  quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo
  consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse
  cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non
  proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")

(def lorem-ipsum-2
  "Curabitur pretium tincidunt lacus. Nulla gravida orci a odio. Nullam varius,
  turpis et commodo pharetra, est eros bibendum elit, nec luctus magna felis
  sollicitudin mauris. Integer in mauris eu nibh euismod gravida. Duis ac tellus
  et risus vulputate vehicula. Donec lobortis risus a elit. Etiam tempor. Ut
  ullamcorper, ligula eu tempor congue, eros est euismod turpis, id tincidunt
  sapien risus a quam. Maecenas fermentum consequat mi. Donec fermentum.
  Pellentesque malesuada nulla a mi. Duis sapien sem, aliquet nec, commodo eget,
  consequat quis, neque. Aliquam faucibus, elit ut dictum aliquet, felis nisl
  adipiscing sapien, sed malesuada diam lacus eget erat. Cras mollis scelerisque
  nunc. Nullam arcu. Aliquam consequat. Curabitur augue lorem, dapibus quis,
  laoreet et, pretium ac, nisi. Aenean magna nisl, mollis quis, molestie eu,
  feugiat in, orci. In hac habitasse platea dictumst.")

(defn padded
  [element]
  [:div {:style {:padding 10}}
   element])

(def tabs-big
  [["Lorem ipsum" [padded
                   [:p lorem-ipsum-1]
                   [:p lorem-ipsum-2]]]
   ["Something else" [padded
                      [:h1 "A title"]
                      [:p "Something entirely different"]]]
   ["Third tab" [padded
                 [:h1 "More lorem ipsum"]
                 [:p lorem-ipsum-1]]]
   ["Fourth" [padded
              [:h1 "Even more lorem ipsum!!!"]
              [:p lorem-ipsum-2]]]
   ["Fifth" [padded
             [:h1 "Even more lorem ipsum!!!"]
             [:p lorem-ipsum-2]]]
   ["Sixth" [padded
             [:h1 "Even more lorem ipsum!!!"]
             [:p lorem-ipsum-2]]]])

(def tabs-small
  [["1" [padded "One"]]
   ["2" [padded "Two"]]
   ["3" [padded "Three"]]
   ["4" [padded "Four"]]])

(defonce tabs-ratom
  (r/atom {:kvs (tabs/heterostyled tabs-big)
           :i   0}))

(defonce tabs-ratom-for-cursor
  (r/atom {:a {:b {:c {:kvs tabs-small
                       :i   2}}}}))

(defonce tabs-cursor
  (r/cursor tabs-ratom-for-cursor [:a :b :c]))


(defonce tabs-ratom-for-reaction
  (r/atom {:kvs tabs-small
           :i   1}))

(defonce tabs-reaction
  (ratom/make-reaction #(deref tabs-ratom-for-reaction)
                       :on-set (fn [_ v] (reset! tabs-ratom-for-reaction v))))


(defonce tabs-ratom-for-wrapper
  (r/atom {:kvs tabs-small
           :i   1}))

(def landmarks
  {:banner        [landmarks/banner
                   [landmarks/search {:aria-label "ib"}
                    "banner > search"]]
   :complementary [landmarks/complementary {:aria-label "john"}
                   [landmarks/form {:aria-label "karsten"}
                    "complementary > form"]]
   :content-info  [landmarks/content-info
                   [landmarks/region {:aria-label "palle"}
                    "contentinfo > region"]]
   :main          [landmarks/main
                   [landmarks/navigation {:aria-label "ludvig"}
                    "main > navigation"]]})

(defonce code-lens-state
  (r/atom nil))

(defn app
  []
  [:<>
   [lens/code code-lens-state]

   #_[layout/root landmarks]

   ;; Using ratom as state.
   [tabs tabs-ratom {:tab-list-id "ratom"}]
   [:br]

   ;; Using cursor as state.
   #_[:pre
      "cursor: " (with-out-str (pprint @tabs-cursor))
      "original ratom: \n" (with-out-str (pprint @tabs-ratom-for-cursor))]
   [tabs tabs-cursor {:tab-list-id "cursor"}]
   [:br]

   ;; Using reaction as state.
   #_[:pre
      "reaction ratom: " (with-out-str (pprint @tabs-reaction))
      "original ratom: " (with-out-str (pprint @tabs-ratom-for-reaction))]
   [tabs tabs-reaction {:tab-list-id "reaction"}]
   [:br]

   ;; Using wrap as state.
   #_[:pre
      "wrapper ratom: " (with-out-str (pprint @tabs-ratom-for-wrapper))
      "original ratom: " (with-out-str (pprint @tabs-ratom-for-wrapper))]
   [tabs (r/wrap @tabs-ratom-for-wrapper
                 reset! tabs-ratom-for-wrapper)
    {:tab-list-id "wrapper"}]
   [:br]])

(def root
  (js/document.getElementById "app"))

(defn ^:dev/after-load render
  []
  (rdom/render [app] root))

(defn start-dev
  []
  (println "Started development environment for kuhumcst/recap.")
  (render))
