(ns user
  (:require [clojure.pprint :refer [pprint]]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [reagent.ratom :as ratom]
            [dk.cst.stucco.util.state :as state]
            [dk.cst.stucco.landmark :as landmark]
            [dk.cst.stucco.group :as group]
            [dk.cst.stucco.pattern :as pattern]
            [dk.cst.stucco.document :as doc]))

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

;; When placing components inside tab content it is important to provide a
;; reference to an atom! Otherwise the state will not be preserved between tab
;; changes, i.e. the index of a carousel will get reset every time the tab is
;; repainted. Providing atom - inlined or external - preserves the state.

(defonce fascimile-text
  (r/atom {:i   0
           :kvs [["Side 1" [:<>
                            [:p lorem-ipsum-1]
                            [:p lorem-ipsum-2]]]
                 ["Side 2" [:<>
                            [:p lorem-ipsum-2]
                            [:p lorem-ipsum-1]]]
                 ["Side 3" [:<>
                            [:p lorem-ipsum-1]]]]}))

(def tabs-big
  [["First" ^{:key (random-uuid)} [pattern/carousel fascimile-text
                                   {:aria-label "Test"}]]
   ["Second" ^{:key (random-uuid)} [pattern/carousel {:i   0
                                                      :kvs [[1 1]]}]]
   ["Third" [:<>
             [:h1 "More lorem ipsum"]
             [:p lorem-ipsum-1]]]
   #_["Fourth" [:<>
                [:h1 "Even more lorem ipsum!!!"]
                [:p lorem-ipsum-2]]]
   #_["Fifth" [:<>
               [:h1 "Even more lorem ipsum!!!"]
               [:p lorem-ipsum-2]]]
   #_["Sixth" [:<>
               [:h1 "Even more lorem ipsum!!!"]
               [:p lorem-ipsum-2]]]])

(def tabs-small
  [["1" "One"]
   ["2" "Two"]
   ["3" "Three"]
   ["4" "Four"]])

(defonce tabs-ratom
  (r/atom {:kvs (pattern/heterostyled tabs-big shuffle)
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
  {:banner        [landmark/banner {:style {:background "yellow"}}
                   "banner"
                   [landmark/search {:aria-label "search label"
                                     :style      {:background "orange"}}
                    "search"]]
   :complementary [landmark/complementary {:aria-label "complementary label"
                                           :style      {:background "red"}}
                   "complementary"
                   [landmark/form {:aria-label "form label"
                                   :style      {:background "brown"}}
                    "form"]]
   :content-info  [landmark/content-info {:style {:background "grey"}}
                   "contentinfo"
                   [landmark/region {:aria-label "region label"
                                     :style      {:background "lightgrey"}}
                    "region"]]
   :main          [landmark/main {:style {:background "green"}}
                   "main"
                   [landmark/navigation {:aria-label "navigation label"
                                         :style      {:background "lightgreen"}}
                    "navigation"]]})

(defonce code-lens-state
  (r/atom nil))

(def facs
  (cycle [[doc/illustration {:src "img/handwriting.jpg"
                             :alt "Illegible handwriting"}]
          [doc/illustration {:src   "img/handwriting.jpg"
                             :alt   "Illegible handwriting"
                             :style {:filter "invert(100%)"}}]
          [doc/illustration {:src   "img/handwriting.jpg"
                             :alt   "Illegible handwriting"
                             :style {:filter "sepia(100%)"}}]]))

;; Re-frame subscription-like reaction that only updates on the :i key and
;; intercepts the :kvs value before rendering.
(def facsimile-img
  (let [ks  (map first (:kvs @fascimile-text))
        kvs (pattern/heterostyled (map vector ks facs))]
    (state/derive fascimile-text {:kvs kvs})))

(defn app
  []
  [:<>
   [landmark/root landmarks]
   [:br]

   ;; For testing drag-and-drop between descendants/ancestors.
   [pattern/tabs (r/atom {:i   0
                          :kvs [[1 [pattern/tabs (r/atom {:i   2
                                                          :kvs [[1 "testing"] [2 "a"] [3 "ratom"]]})]]
                                [2 "something"]
                                [3 "glen"]]})]
   [:br]

   [group/combination
    {:vs      [[pattern/carousel facsimile-img]
               [pattern/tabs tabs-ratom {:id "ratom"}]]
     :weights [1 1]}]
   [pattern/code code-lens-state]
   [pattern/carousel {:i   0
                      :kvs [[1 [:<>
                                [:p lorem-ipsum-1]
                                [:p lorem-ipsum-2]]]
                            [2 lorem-ipsum-1]
                            [3 lorem-ipsum-2]]}]
   [:br]
   [pattern/carousel (r/atom {:i   2
                              :kvs [[1 "testing"] [2 "a"] [3 "ratom"]]})]
   [:br]

   ;; Using ratom as state.
   ;[tabs tabs-ratom {:id "ratom"}]
   ;[:br]

   ;; Using cursor as state.
   #_[:pre
      "cursor: " (with-out-str (pprint @tabs-cursor))
      "original ratom: \n" (with-out-str (pprint @tabs-ratom-for-cursor))]
   [pattern/tabs tabs-cursor {:id "cursor"}]
   [:br]

   ;; Using reaction as state.
   #_[:pre
      "reaction ratom: " (with-out-str (pprint @tabs-reaction))
      "original ratom: " (with-out-str (pprint @tabs-ratom-for-reaction))]
   [pattern/tabs tabs-reaction {:id "reaction"}]
   [:br]

   ;; Using wrap as state.
   #_[:pre
      "wrapper ratom: " (with-out-str (pprint @tabs-ratom-for-wrapper))
      "original ratom: " (with-out-str (pprint @tabs-ratom-for-wrapper))]
   [pattern/tabs (r/wrap @tabs-ratom-for-wrapper
                         reset! tabs-ratom-for-wrapper)
    {:id "wrapper"}]
   [:br]])

(def root
  (js/document.getElementById "app"))

(defn ^:dev/after-load render
  []
  (rdom/render [app] root))

(defn start-dev
  []
  (println "Started development environment for kuhumcst/stucco.")
  (render))
