(ns kuhumcst.recap.tabs
  "Reagent components for creating a tabbed UI.

  Shared state for tab components:
    `kvs` - key-value pairs of tab labels and bodies.
    `i`   - (optional) the index of the currently selected tab.

  Shared opts for tab components:
    `tab-list-id` - a unique id attribute for the tab-list.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#tabpanel"
  (:require [kuhumcst.recap.drag :as rd]
            [kuhumcst.recap.state :as state]
            [kuhumcst.recap.util :as util]))

;; TODO: fix - selecting a tab resets focus to an earlier tab in the list
;;       actually, this is OK, but tabs should be selected using arrow keys
;;       FIX: a focus listener stores focus as a stack. Universally restore
;;       previous focus when focus is lost from a component and the component is
;;       no longer in the DOM? Will restore to either first element available in
;;       stack, either based on DOM object or ID (in case the object has been
;;       swapped out).

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
  [tab-list-id n]
  (str tab-list-id "-" n))

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
  [state {:keys [tab-list-id] :as opts}]
  (state/assert-conforms ::state/kvs+i state)
  (let [{:keys [kvs i] :or {i 0}} @state
        length (count kvs)
        append (fn [kv]
                 ;; Internal drops will have no increase in tab count, so when
                 ;; appending inside the same tab-list we must account for it.
                 (if (= tab-list-id (:tab-list-id (meta kv)))
                   (swap! state mk-drop-state (dec length) kv)
                   (swap! state mk-drop-state length kv)))]
    [:div.tab-list {:id   tab-list-id
                    :role "tab-list"}
     (for [n (range length)
           :let [kv     (vary-meta (nth kvs n) assoc
                                   :tab-list-id tab-list-id
                                   :selected? (= n i))
                 id     (mk-tab-id tab-list-id n)
                 delete (fn []
                          (swap! state mk-drag-state n)
                          kv)
                 insert (fn [kv]
                          (swap! state mk-drop-state n kv))
                 select (fn []
                          (swap! state assoc :i n))]]
       ;; Would prefer using button, but FF excludes its padding from drag area.
       [:span.tab (merge (util/tab-attr select)
                         {:key           (hash [kvs i n])
                          :id            id
                          :style         (:style (meta kv))
                          :aria-selected (:selected? (meta kv))
                          :draggable     true
                          :on-drag-start (rd/on-drag-start delete)
                          :on-drag-over  (rd/on-drag-over)
                          :on-drop       (rd/on-drop insert)})
        (first kv)])
     [:span.tab-dropzone {:on-drag-over (rd/on-drag-over)
                          :on-drop      (rd/on-drop append)}]]))

(defn tab-panel
  "The currently selected tab-panel of the `state`."
  [state {:keys [tab-list-id] :as opts}]
  (state/assert-conforms ::state/kvs+i state)
  (let [{:keys [kvs i] :or {i 0}} @state
        [_ v :as kv] (when (not-empty kvs)
                       (nth kvs i))]
    (when v
      [:section.tab-panel {:aria-labelledby (mk-tab-id tab-list-id i)
                           :style           (:style (meta kv))}
       v])))

(defn tabs
  "A merged view of the tab-list and the tab-panel of the currently selected tab.
  Takes `state` of the form described in the docstring of this namespace."
  [state {:keys [tab-list-id] :as opts}]
  [:article.tabs
   [tab-list state opts]
   [tab-panel state opts]])
