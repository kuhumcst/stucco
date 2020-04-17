(ns kuhumcst.recap.tabs
  "Reagent components for creating a tabbed UI.

  Shared state for tab components:
    `tabs` - key-value pairs of tab labels and bodies.
    `i`    - (optional) the index of the currently selected tab.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#tabpanel"
  (:require [kuhumcst.recap.drag :as rd]
            [kuhumcst.recap.util :as util]))

;; TODO: fix - selecting a tab resets focus to the first tab in the form
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

(defn tablist
  "The tabs available in the `state`."
  [state]
  (let [form-id (random-uuid)]
    (fn [state]
      (let [{:keys [tabs i] :or {i 0}} @state
            append (fn [tab]
                     (if (= form-id (:form-id (meta tab)))
                       (swap! state mk-drop-state (dec (count tabs)) tab)
                       (swap! state mk-drop-state (count tabs) tab)))]
        [:form.tablist
         (for [n (range (count tabs))
               :let [[k _ :as tab] (with-meta (nth tabs n)
                                              {:form-id   form-id
                                               :selected? (= n i)})
                     delete (fn []
                              (swap! state mk-drag-state n tab)
                              tab)
                     insert (fn [tab]
                              (swap! state mk-drop-state n tab))
                     select (fn []
                              (swap! state assoc :i n))]]
           [:<> {:key (hash [tabs i n])}
            [:input {:tab-index       -1
                     :type            "radio"
                     :default-checked (:selected? (meta tab))}]
            [:label (merge (util/tab-attr select)
                           {:draggable     true
                            :on-drag-start (rd/on-drag-start delete)
                            :on-drag-over  (rd/on-drag-over)
                            :on-drop       (rd/on-drop insert)})
             k]])
         [:span.tablist__spacer {:on-drag-over (rd/on-drag-over)
                                 :on-drop      (rd/on-drop append)}
          "[+ tab]"]]))))

(defn tabpanel
  "The currently selected tabpanel of the `state`."
  [state]
  (let [{:keys [tabs i] :or {i 0}} @state
        [_ v] (when (not-empty tabs)
                (nth tabs i))]
    [:section.tabpanel v]))

(defn tabs
  "A merged view of the tablist and the tabpanel of the currently selected tab.
  Takes `state` of the form described in the docstring of this namespace."
  [state]
  [:article.tabs
   [tablist state]
   [tabpanel state]])
