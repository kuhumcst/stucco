(ns user
  (:require [clojure.pprint :refer [pprint]]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [reagent.ratom :as ratom]
            [dk.cst.stucco.state :as state]
            [dk.cst.stucco.foundation :as foundation]
            [dk.cst.stucco.surface :as doc]
            [dk.cst.stucco.plastic :refer [carousel tabs] :as plastic]))

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
  [["First" ^{:key (random-uuid)} [carousel fascimile-text
                                   {:aria-label "Test"}]]
   ["Second" ^{:key (random-uuid)} [carousel {:i   0
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
  (r/atom {:kvs (plastic/heterostyled tabs-big shuffle)
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
  {:banner        [foundation/banner
                   [foundation/search {:aria-label "ib"}
                    "banner > search"]]
   :complementary [foundation/complementary {:aria-label "john"}
                   [foundation/form {:aria-label "karsten"}
                    "complementary > form"]]
   :content-info  [foundation/content-info
                   [foundation/region {:aria-label "palle"}
                    "contentinfo > region"]]
   :main          [foundation/main
                   [foundation/navigation {:aria-label "ludvig"}
                    "main > navigation"]]})

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
  (let [ks (map first (:kvs @fascimile-text))]
    (state/derive fascimile-text {:kvs (map vector ks facs)})))

(defn app
  []
  [:<>
   [foundation/combination {:vs  [[carousel facsimile-img]
                                  [tabs tabs-ratom {:id "ratom"}]]
                            :weights [1 1]}]
   [plastic/code code-lens-state]
   [carousel {:i   0
              :kvs [[1 [:<>
                        [:p lorem-ipsum-1]
                        [:p lorem-ipsum-2]]]
                    [2 lorem-ipsum-1]
                    [3 lorem-ipsum-2]]}]
   [:br]
   [carousel (r/atom {:i   2
                      :kvs [[1 "testing"] [2 "a"] [3 "ratom"]]})]
   [:br]

   #_[foundation/root landmarks]

   ;; Using ratom as state.
   ;[tabs tabs-ratom {:id "ratom"}]
   ;[:br]

   ;; Using cursor as state.
   #_[:pre
      "cursor: " (with-out-str (pprint @tabs-cursor))
      "original ratom: \n" (with-out-str (pprint @tabs-ratom-for-cursor))]
   [tabs tabs-cursor {:id "cursor"}]
   [:br]

   ;; Using reaction as state.
   #_[:pre
      "reaction ratom: " (with-out-str (pprint @tabs-reaction))
      "original ratom: " (with-out-str (pprint @tabs-ratom-for-reaction))]
   [tabs tabs-reaction {:id "reaction"}]
   [:br]

   ;; Using wrap as state.
   #_[:pre
      "wrapper ratom: " (with-out-str (pprint @tabs-ratom-for-wrapper))
      "original ratom: " (with-out-str (pprint @tabs-ratom-for-wrapper))]
   [tabs (r/wrap @tabs-ratom-for-wrapper
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
