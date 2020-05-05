(ns kuhumcst.recap.widgets.tabs
  "Reagent components for creating a tabbed UI.

  Shared state for tab components:
    :kvs - key-value pairs of tab labels and bodies.
    :i   - (optional) the index of the currently selected tab.

  Various opts for tab components:
    :id - a unique id attribute for the tab-list.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#tabpanel"
  (:require [kuhumcst.recap.dom.focus :as focus]
            [kuhumcst.recap.dom.drag :as drag]
            [kuhumcst.recap.dom.keyboard :as kbd]
            [kuhumcst.recap.state :as state]
            [kuhumcst.recap.util :as util]))

;; TODO: find some way to remove dropzone when full width of tabs
;; TODO: complete a11y

(defn- mk-drag-state
  [{:keys [kvs i] :or {i 0}} n]
  {:kvs (util/vec-dissoc kvs n)
   :i   (cond
          (= n i) (min i (- (count kvs) 2))                 ; go right
          (< n i) (dec i)                                   ; go left
          (> n i) i)})                                      ; stay in place

(defn- mk-drop-state
  [{:keys [kvs i] :or {i 0}} n kv]
  {:kvs (util/vec-assoc kvs n kv)
   :i   (cond
          (:selected? (meta kv)) n                          ; go to dropped kv
          (= n i) (inc i)                                   ; go right
          (< n i) (inc i)                                   ; go right
          (> n i) (max 0 i))})                              ; stay in place

(defn- mk-tab-id
  [parent-id n]
  (str parent-id "-" n))

(defn heterostyled
  "Apply heterogeneous styling to tab `kvs`."
  [kvs]
  (let [backgrounds (cycle (shuffle ["var(--tab-background-1)"
                                     "var(--tab-background-2)"
                                     "var(--tab-background-3)"
                                     "var(--tab-background-4)"
                                     "var(--tab-background-5)"
                                     "var(--tab-background-6)"]))
        mk-style    (fn [m n]
                      (assoc m :style {:background (nth backgrounds n)}))]
    (into (empty kvs)
          (map-indexed (fn [n kv]
                         (vary-meta kv mk-style n))
                       kvs))))

(defn tab-list
  "The tabs available in the `state`."
  [{:keys [kvs i] :as state}
   {:keys [id] :as opts}]
  (state/assert-conforms ::state/kvs+i state)
  (let [{:keys [kvs i] :or {i 0}} @state
        length (count kvs)
        append (fn [kv]
                 ;; Internal drops will have no increase in tab count, so when
                 ;; appending inside the same tab-list we must account for it.
                 (if (= id (:id (meta kv)))
                   (swap! state mk-drop-state (dec length) kv)
                   (swap! state mk-drop-state length kv)))]
    [:div.tab-list {:role        "tablist"
                    :id          id
                    :on-key-down kbd/roving-tabindex-handler}
     (for [n (range length)
           :let [kv        (nth kvs n)
                 selected? (= n i)
                 tab-id    (mk-tab-id id n)
                 delete    (fn []
                             (swap! state mk-drag-state n)
                             (vary-meta kv assoc
                                        :id id
                                        :selected? selected?))
                 insert    (fn [kv]
                             (swap! state mk-drop-state n kv))
                 select    (fn []
                             (swap! state assoc :i n))]]
       ;; Would prefer using button, but FF excludes its padding from drag area.
       [:span.tab {:role          "tab"
                   :key           (hash [kvs i n])
                   :id            tab-id
                   :ref           focus/accept!
                   :style         (:style (meta kv))
                   :aria-selected selected?
                   :tab-index     (if selected? "0" "-1")
                   :auto-focus    selected?
                   :on-click      select
                   :draggable     true
                   :on-drag-start (drag/on-drag-start delete)
                   :on-drag-end   (drag/on-drag-end)
                   :on-drag-enter (drag/on-drag-enter)
                   :on-drag-over  (drag/on-drag-over)
                   :on-drag-leave (drag/on-drag-leave)
                   :on-drop       (drag/on-drop insert)}
        (first kv)])
     [:span.tab-dropzone {:on-drag-enter (drag/on-drag-enter)
                          :on-drag-over  (drag/on-drag-over)
                          :on-drag-leave (drag/on-drag-leave)
                          :on-drop       (drag/on-drop append)}]]))

(defn tab-panel
  "The currently selected tab-panel of the `state`."
  [{:keys [kvs i] :as state}
   {:keys [id] :as opts}]
  (state/assert-conforms ::state/kvs+i state)
  (let [{:keys [kvs i] :or {i 0}} @state
        [_ v :as kv] (when (not-empty kvs)
                       (nth kvs i))]
    (when v
      [:section.tab-panel {:role            "tabpanel"
                           :aria-labelledby (mk-tab-id id i)
                           :style           (:style (meta kv))}
       v])))

(defn tabs
  "Merged view of the tab-list and the tab-panel of the currently selected tab.
  Takes `state` of the form described in the docstring of this namespace."
  [{:keys [kvs i] :as state}
   {:keys [id] :as opts}]
  [:article.tabs
   [tab-list state opts]
   [tab-panel state opts]])
