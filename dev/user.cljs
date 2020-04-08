(ns user
  (:require [clojure.pprint :refer [pprint]]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [kuhumcst.recap.tab :as tab]))

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

(defonce kvs
  (r/atom [["Lorem ipsum" [:<>
                           [:p lorem-ipsum-1]
                           [:p lorem-ipsum-2]]]
           ["Something else" [:<>
                              [:h1 "A title"]
                              [:p "Something entirely different"]]]
           ["Third tab" [:<>
                         [:h1 "More lorem ipsum"]
                         [:p lorem-ipsum-1]]]
           ["Fourth" [:<>
                      [:h1 "Even more lorem ipsum!!!"]
                      [:p lorem-ipsum-2]]]]))

(defonce i-ratom
  (r/atom 0))

(defonce i-map
  (r/atom 0))

(defonce i-meta
  (r/atom 0))

(defn app
  []
  [:<>
   ;; Using a ratoms as state.
   [tab/window {:kvs kvs
                :i   i-ratom}]
   [:br]

   ;; Dispatching on maps.
   [tab/window {:kvs {:deref  #(deref kvs)
                      :reset! #(reset! kvs %)
                      :swap!  #(swap! kvs % %)}
                :i   {:deref  #(deref i-map)
                      :reset! #(reset! i-map %)
                      :swap!  #(swap! i-map % %)}}]
   [:br]

   ;; Dispatching on metadata.
   [tab/window {:kvs ^{:deref  #(deref kvs)
                       :reset! #(reset! kvs %)
                       :swap!  #(swap! kvs % %)} [:n/a]
                :i ^{:deref  #(deref i-meta)
                     :reset! #(reset! i-meta %)
                     :swap!  #(swap! i-meta % %)} [:n/a]}]])

(def root
  (js/document.getElementById "app"))

(defn ^:dev/after-load render
  []
  (rdom/render [app] root))

(defn start-dev
  []
  (println "Started development environment for kuhumcst/recap.")
  (render))
