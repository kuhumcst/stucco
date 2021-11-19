(ns dk.cst.stucco.pattern
  "Reagent component implementations of common WAI-ARIA patterns.

  These components try to match the WAI-ARIA example patterns as closely as
  possible. The implementations include common interface elements such as
  carousels and tabbed panels.  Ideally, these comprise the second (or a lower)
  level in a layout, with the top level composed of WAI-ARIA landmarks
  (available in the 'dk.cst.stucco.landmark' namespace).

  For looser groupings, take a look at the 'dk.cst.stucco.group' namespace.

  ARIA references:
    https://www.w3.org/TR/wai-aria-practices-1.1/#aria_ex"
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [clojure.pprint :refer [pprint]]
            [reagent.core :as r]
            [dk.cst.stucco.util.state :as state]
            [dk.cst.stucco.dom.keyboard :as kbd]
            [dk.cst.stucco.dom.focus :as focus]
            [dk.cst.stucco.dom.drag :as drag]))

;;;; TABS
(defn- mk-tab-id
  [parent-id n]
  (str parent-id "-" n))

(defn- mk-tab-panel-id
  [parent-id n]
  (str parent-id "-tabpanel-" n))

(def background-colours
  ["var(--tab-background-1)"
   "var(--tab-background-2)"
   "var(--tab-background-3)"
   "var(--tab-background-4)"
   "var(--tab-background-5)"
   "var(--tab-background-6)"])

(defn heterostyled
  "Apply heterogeneous styling to tab `kvs`."
  ([kvs]
   (heterostyled kvs identity))
  ([kvs order-fn]
   (let [backgrounds (cycle (order-fn background-colours))
         mk-style    (fn [m n]
                       (assoc m :style {:background (nth backgrounds n)}))]
     (into (empty kvs)
           (map-indexed (fn [n kv]
                          (vary-meta kv mk-style n))
                        kvs)))))

;; TODO: what to do when drag-and-dropping from tabs using same state?
;; Currently, the two tabs components have their tabs reordered, but should
;; they duplicate the affected tab instead?
(defn- tab-list
  "The tabs available in the `state`."
  [{:keys [kvs i] :as state}
   {:keys [id] :as opts}]
  (state/assert-valid state ::state/kvs+i)
  (let [{:keys [kvs i] :or {i 0}} @state
        panel-id (mk-tab-panel-id id i)
        length   (count kvs)
        append   (fn [kv]
                   ;; Internal drops will have no increase in tab count, so when
                   ;; appending inside the same tab-list we must account for it.
                   (if (= id (:id (meta kv)))
                     (swap! state state/mk-drop-state (dec length) kv)
                     (swap! state state/mk-drop-state length kv)))]
    [:div.tab-list {:role          "tablist"
                    :aria-label    "Choose a tab to display" ;TODO: localisation
                    :id            id
                    :on-key-down   kbd/roving-tabindex-handler
                    :on-drag-enter drag/on-drag-enter
                    :on-drag-over  drag/on-drag-over
                    :on-drag-leave drag/on-drag-leave
                    :on-drop       (drag/on-drop append)}
     (for [n (range length)
           :let [[k _ :as kv] (nth kvs n)
                 selected? (= n i)
                 tab-id    (mk-tab-id id n)
                 source-id (mk-tab-panel-id id n)           ; drag container
                 delete    (fn []
                             (swap! state state/mk-drag-state n)
                             (vary-meta kv assoc
                                        :id id
                                        :selected? selected?))
                 insert    (fn [kv]
                             (swap! state state/mk-drop-state n kv))
                 select    (fn []
                             (swap! state assoc :i n))]]
       ;; Would prefer using button, but FF excludes its padding from drag area.
       [:span.tab {:role          "tab"
                   :key           (hash [kvs i n])
                   :id            tab-id
                   :ref           focus/accept!
                   :style         (:style (meta kv))
                   :aria-selected selected?
                   :aria-controls panel-id
                   :aria-label    (str "View tab number " (inc n)) ;TODO: localisation
                   :tab-index     (if selected? "0" "-1")
                   :auto-focus    selected?
                   :on-click      select
                   :draggable     true
                   :on-drag-start (drag/on-drag-start delete source-id)
                   :on-drag-end   drag/on-drag-end
                   :on-drag-enter drag/on-drag-enter
                   :on-drag-over  drag/on-drag-over
                   :on-drag-leave drag/on-drag-leave
                   :on-drop       (drag/on-drop insert)}
        k])]))

(defn- tab-panel
  "The currently selected tab-panel of the `state`."
  [{:keys [kvs i] :as state}
   {:keys [id] :as opts}]
  (state/assert-valid state ::state/kvs+i)
  (let [{:keys [kvs i] :or {i 0}} @state
        [_ v :as kv] (when (not-empty kvs)
                       (nth kvs i))]
    (when v
      [:section.tab-panel {:role            "tabpanel"
                           :id              (mk-tab-panel-id id i)
                           :aria-labelledby (mk-tab-id id i)
                           :style           (:style (meta kv))}
       v])))

(defn tabs
  "Merged view of the tab-list and the tab-panel of the currently selected tab.

  Takes `state` of the form:
    :kvs - key-value pairs of tab labels and bodies.
    :i   - (optional) the index of the currently selected tab.

  Various opts for tab components:
    :id - (optional) a unique id attribute for the tab-list.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#tabpanel"
  [{:keys [kvs i] :as state}
   {:keys [id] :as opts}]
  (let [state (state/prepare ::state/kvs+i state)
        opts  (assoc opts :id (or id (random-uuid)))]
    [:article.tabs
     [tab-list state opts]
     [tab-panel state opts]]))


;;;; CAROUSEL

;; TODO: drag-and-drop
(defn carousel
  "Tabbed carousel with a slide picker, but without automatic slide rotation.

  Takes `state` of the form:
    :kvs  - key-values pairs of slide labels and bodies.
    :i    - (optional) the index of the currently selected slide.

  Optionally, certain HTML attributes specified in the `opts` may merged with
  the carousel attr. This should be done in order to satisfy ARIA labeling
  requirements, e.g. either :aria-label or :aria-labelledby should be set.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#carousel"
  [{:keys [kvs i] :as state}
   {:keys [aria-label
           aria-labelledby]
    :as   opts}]
  (r/with-let [state      (state/prepare ::state/kvs+i state)
               next-slide #(swap! state update :i inc)
               prev-slide #(swap! state update :i dec)]
    (let [{:keys [i kvs]} @state
          styles       (map (comp :style meta) kvs)
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
                                  :on-click   (when prev? prev-slide)
                                  :style      (nth styles (dec i) nil)}]
       [:div.carousel__slide {:role            "tabpanel"
                              :id              tab-panel-id
                              :aria-labelledby label-id
                              :style           (nth styles i)}
        [:div.carousel__slide-header
         [:div.carousel__slide-label {:id label-id} label]
         (when (> (count kvs) 1)
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
                                             :on-click      select}])))]
        [:div.carousel__slide-separator]
        content]
       [:button.carousel__select {:aria-label (str "View slide number " (inc i)) ;TODO: localisation
                                  :tab-index  (if next? "0" "-1")
                                  :on-click   (when next? next-slide)
                                  :style      (nth styles (inc i) nil)}]])))


;;;; INSPECTION

;; TODO: incorrect for any combination of ns and symbol containing $
(defn- f->symbol
  "Approximate the symbol of function `f` from its .-name property."
  [f]
  (let [parts (str/split (.-name f) #"\$")]
    (-> (str/join (interpose "." (butlast parts)))
        (str "/" (last parts))
        (symbol))))

(defn- coll->code
  [coll]
  (walk/postwalk (fn [x]
                   (if (fn? x)
                     (f->symbol x)
                     x))
                 coll))

;; TODO: experiment - can this be an editable text field?
;; TODO: make appendable state, 2-column table view
;; TODO: make default operation copy rather than move
;; TODO: make accessible
(defn code
  "Drop-zone for inspecting data as code. Accepts `state` as optional param."
  [state]
  (let [delete (fn []
                 (when-let [*state @state]
                   (reset! state nil)
                   *state))
        insert (fn [x] (reset! state x))]

    (fn []
      (let [*state @state]
        [:pre {:on-drag-over drag/on-drag-over
               :on-drop      (drag/on-drop insert)}
         (if (empty? *state)
           [:code.code-lens.code-lens--empty "( )"]
           [:code.code-lens {:draggable     true
                             ;; TODO: provide a :source-id
                             :on-drag-start (drag/on-drag-start delete nil)}
            (when-let [metadata (meta *state)]
              [:div.code-lens__meta {:title "Metadata"}
               "^" (with-out-str (pprint (coll->code metadata)))])
            (when *state
              (with-out-str (pprint (coll->code *state))))])]))))
