(ns kuhumcst.recap.tabs
  "Reagent components for creating a tabbed UI.

  Shared state for tab components:
    `tabs` - key-value pairs of tab labels and bodies.
    `i`    - (optional) the index of the currently selected tab.

  Shared opts for tab components:
    `tab-list-id` - a unique id attribute for the tab-list.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#tabpanel"
  (:require [kuhumcst.recap.drag :as rd]
            [kuhumcst.recap.util :as util]))

;; TODO: fix issue with button padding area not being draggable (only in FF)
;; TODO: fix - selecting a tab resets focus to an earlier tab in the list
;;       actually, this is OK, but tabs should be selected using arrow keys
;; TODO: deterministic random background colour of the tab labels
;;       cycle through a set of standard colours and set as metadata on key-value pair

(defn- mk-drag-state
  [{:keys [tabs i] :or {i 0}} n tab]
  {:tabs (util/vec-dissoc tabs n)
   :i    (cond
           (:selected? (meta tab)) 0
           (< n i) (dec i)
           :else i)})

(defn- mk-drop-state
  [{:keys [tabs i] :or {i 0}} n tab]
  {:tabs (util/vec-assoc tabs n tab)
   :i    (cond
           (:selected? (meta tab)) n
           (<= n i) (inc i)
           :else i)})

(defn- mk-tab-id
  [tab-list-id n]
  (str tab-list-id "-" n))

(defn tab-list
  "The tabs available in the `state`."
  [state {:keys [tab-list-id] :as opts}]
  (let [{:keys [tabs i] :or {i 0}} @state
        append (fn [tab]
                 (if
                   (swap! state mk-drop-state (dec (count tabs)) tab)
                   (swap! state mk-drop-state (count tabs) tab)))]
    [:div.tab-list {:id   tab-list-id
                    :role "tab-list"}
     (for [n (range (count tabs))
           :let [[k _ :as tab] (with-meta (nth tabs n)
                                          {:tab-list-id tab-list-id
                                           :selected?   (= n i)})
                 id     (mk-tab-id tab-list-id n)
                 delete (fn []
                          (swap! state mk-drag-state n tab)
                          tab)
                 insert (fn [tab]
                          (swap! state mk-drop-state n tab))
                 select (fn []
                          (swap! state assoc :i n))]]
       ;; Would prefer using button, but FF excludes its padding from drag area.
       [:span.tab (merge (util/tab-attr select)
                         {:key           (hash [tabs i n])
                          :id            id
                          :aria-selected (:selected? (meta tab))
                          :draggable     true
                          :on-drag-start (rd/on-drag-start delete)
                          :on-drag-over  (rd/on-drag-over)
                          :on-drop       (rd/on-drop insert)})
        k])
     [:span.tab-list__spacer {:on-drag-over (rd/on-drag-over)
                              :on-drop      (rd/on-drop append)}]]))

(defn tab-panel
  "The currently selected tab-panel of the `state`."
  [state {:keys [tab-list-id] :as opts}]
  (let [{:keys [tabs i] :or {i 0}} @state
        [_ v] (when (not-empty tabs)
                (nth tabs i))
        tab-id (mk-tab-id tab-list-id i)]
    [:section.tab-panel {:aria-labelledby tab-id}
     v]))

(defn tabs
  "A merged view of the tab-list and the tab-panel of the currently selected tab.
  Takes `state` of the form described in the docstring of this namespace."
  [state {:keys [tab-list-id] :as opts}]
  [:article.tabs
   [tab-list state opts]
   [tab-panel state opts]])
