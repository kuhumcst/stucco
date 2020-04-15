(ns kuhumcst.recap.tab
  "Reagent components for creating a tabbed UI.

  Shared state of the tab components:

    `tabs` - key-value pairs of tab labels and bodies.
    `i`    - (optional) the index of the currently selected tab."
  (:require [kuhumcst.recap.drag :as rd]
            [kuhumcst.recap.util :as util]))

;; TODO: fix - selecting a tab resets focus to the first tab in the form
;; TODO: deterministic random background colour of the tab labels
;;       cycle through a set of standard colours and set as metadata on key-value pair

(defn- drag-tab
  [{:keys [tabs i] :or {i 0}} n tab]
  {:tabs (util/vec-dissoc tabs n)
   :i    (cond
           (:selected? (meta tab)) 0
           (< n i) (dec i)
           :else i)})

(defn- drop-tab
  [{:keys [tabs i] :or {i 0}} n tab]
  {:tabs (util/vec-assoc tabs n tab)
   :i    (cond
           (:selected? (meta tab)) n
           (<= n i) (inc i)
           :else i)})

(defn head
  "The head of labels available in the tabs `state`."
  [state]
  (let [form-id (random-uuid)]
    (fn [state]
      (let [{:keys [tabs i] :or {i 0}} @state
            append (fn [tab]
                     (if (= form-id (:form-id (meta tab)))
                       (swap! state drop-tab (dec (count tabs)) tab)
                       (swap! state drop-tab (count tabs) tab)))]
        [:form.tab-head
         (for [n (range (count tabs))
               :let [[k _ :as tab] (with-meta (nth tabs n)
                                              {:form-id   form-id
                                               :selected? (= n i)})
                     delete (fn []
                              (swap! state drag-tab n tab)
                              tab)
                     insert (fn [tab]
                              (swap! state drop-tab n tab))
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
         [:span.tab-head__spacer {:on-drag-over (rd/on-drag-over)
                                  :on-drop      (rd/on-drop append)}
          "[+ tab]"]]))))

(defn body
  "The currently selected body in the tabs `state`."
  [state]
  (let [{:keys [tabs i] :or {i 0}} @state
        [_ v] (when (not-empty tabs)
                (nth tabs i))]
    [:section.tab-body v]))

(defn full
  "A merged view of the tab headers and the body of the currently selected tab.
  Takes tabs `state` of the form described in the docstring of this namespace."
  [state]
  [:article.tab-combined
   [head state]
   [body state]])
