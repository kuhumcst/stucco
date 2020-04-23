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
;; TODO: deterministic random background colour of the tab labels
;;       cycle through a set of standard colours and set as metadata on key-value pair

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

(defn tab-list
  "The tabs available in the `state`."
  [state {:keys [tab-list-id] :as opts}]
  (state/assert-conforms ::state/kvs+i state)
  (let [{:keys [kvs i] :or {i 0}} @state
        append (fn [kv]
                 ;; Internal drops will have no increase in tab count, so when
                 ;; appending inside the same tab-list we must account for it.
                 (if (= tab-list-id (:tab-list-id (meta kv)))
                   (swap! state mk-drop-state (dec (count kvs)) kv)
                   (swap! state mk-drop-state (count kvs) kv)))]
    [:div.tab-list {:id   tab-list-id
                    :role "tab-list"}
     (for [n (range (count kvs))
           :let [kv     (with-meta (nth kvs n)
                                   {:tab-list-id tab-list-id
                                    :selected?   (= n i)})
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
        [_ v] (when (not-empty kvs)
                (nth kvs i))
        tab-id (mk-tab-id tab-list-id i)]
    (when v
      [:section.tab-panel {:aria-labelledby tab-id}
       v])))

(defn tabs
  "A merged view of the tab-list and the tab-panel of the currently selected tab.
  Takes `state` of the form described in the docstring of this namespace."
  [state {:keys [tab-list-id] :as opts}]
  [:article.tabs
   [tab-list state opts]
   [tab-panel state opts]])
