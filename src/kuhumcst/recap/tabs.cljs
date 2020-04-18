(ns kuhumcst.recap.tabs
  "Reagent components for creating a tabbed UI.

  Shared state for tab components:
    `tabs` - key-value pairs of tab labels and bodies.
    `i`    - (optional) the index of the currently selected tab.

  Shared opts for tab components:
    `tablist-id` - a unique id attribute for the tablist.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#tabpanel"
  (:require [kuhumcst.recap.drag :as rd]
            [kuhumcst.recap.util :as util]))

;; TODO: fix issue with button padding area not being draggable
;; TODO: document opts better
;; TODO: fix - selecting a tab resets focus to the first tab in the form
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
  [tablist-id n]
  (str tablist-id "-" n))

(defn tablist
  "The tabs available in the `state`."
  [state {:keys [tablist-id] :as opts}]
  (let [{:keys [tabs i] :or {i 0}} @state
        append (fn [tab]
                 (if
                   (swap! state mk-drop-state (dec (count tabs)) tab)
                   (swap! state mk-drop-state (count tabs) tab)))]
    [:div.tablist {:id   tablist-id
                   :role "tablist"}
     (for [n (range (count tabs))
           :let [[k _ :as tab] (with-meta (nth tabs n)
                                          {:tablist-id tablist-id
                                           :selected?  (= n i)})
                 id     (mk-tab-id tablist-id n)
                 delete (fn []
                          (swap! state mk-drag-state n tab)
                          tab)
                 insert (fn [tab]
                          (swap! state mk-drop-state n tab))
                 select (fn []
                          (swap! state assoc :i n))]]
       [:button.tab {:key           (hash [tabs i n])
                     :on-click      select
                     :id            id
                     :aria-selected (:selected? (meta tab))
                     :draggable     true
                     :on-drag-start (rd/on-drag-start delete)
                     :on-drag-over  (rd/on-drag-over)
                     :on-drop       (rd/on-drop insert)}
        k])
     [:span.tablist__spacer {:on-drag-over (rd/on-drag-over)
                             :on-drop      (rd/on-drop append)}]]))

(defn tabpanel
  "The currently selected tabpanel of the `state`."
  [state {:keys [tablist-id] :as opts}]
  (let [{:keys [tabs i] :or {i 0}} @state
        [k v] (when (not-empty tabs)
                (nth tabs i))
        tab-id (mk-tab-id tablist-id i)]
    [:section.tabpanel {:aria-labelledby tab-id}
     v]))

(defn tabs
  "A merged view of the tablist and the tabpanel of the currently selected tab.
  Takes `state` of the form described in the docstring of this namespace."
  [state {:keys [tablist-id] :as opts}]
  [:article.tabs
   [tablist state opts]
   [tabpanel state opts]])
