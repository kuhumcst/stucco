(ns kuhumcst.recap.tab
  "Reagent components for creating a tabbed UI.

  Shared state of the tab components:

    `tabs` - key-value pairs of tab labels and bodies.
    `i`    - (optional) the index of the currently selected tab."
  (:require [kuhumcst.recap.drag :as rd]
            [kuhumcst.recap.util :as util]))

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

(defn- label
  "The `tab` label at index `n` with relevant `label-attr`."
  [n tab {:keys [id
                 on-drag-start
                 on-drag-over
                 on-drop]
          :as   label-attr}]
  (let [[k _] tab]
    [:<>
     [:input {:id        id
              :type      "radio"
              :checked   (:selected? (meta tab))
              :read-only true
              :value     n}]
     [:label {:for           id
              :draggable     true
              :on-drag-start on-drag-start
              :on-drag-over  on-drag-over
              :on-drop       on-drop}
      k " "]]))

(defn head
  "The head of labels available in the tabs `state`."
  [state]
  (let [{:keys [tabs i] :or {i 0}} (deref state)
        switch-tab #(swap! state assoc :i (js/parseInt (.. % -target -value)))
        form-id    (random-uuid)]
    [:form.tab-head {:on-change switch-tab}
     (for [n (range (count tabs))
           :let [label-id (str form-id "-" n)
                 tab      (with-meta (nth tabs n)
                                     {:form-id   form-id
                                      :selected? (= n i)})
                 drag     (fn []
                            (swap! state drag-tab n tab)
                            tab)
                 drop     (fn [tab]
                            (swap! state drop-tab n tab))]]
       ^{:key label-id} [label n tab {:id            label-id
                                      :on-drag-start (rd/on-drag-start drag)
                                      :on-drag-over  (rd/on-drag-over)
                                      :on-drop       (rd/on-drop drop)}])
     (let [tab->n (fn [tab]
                    ;; Internal move or external append?
                    (if (= form-id (-> tab meta :form-id))
                      (dec (count tabs))
                      (count tabs)))
           append (fn [tab]
                    (swap! state drop-tab (tab->n tab) tab))]
       [:span.tab-head__spacer {:on-drag-over (rd/on-drag-over)
                                :on-drop      (rd/on-drop append)}
        "[+ tab]"])]))

(defn body
  "The currently selected body in the tabs `state`."
  [state]
  (let [{:keys [tabs i] :or {i 0}} (deref state)
        [_ v] (when (not-empty tabs)
                (nth tabs i))]
    [:article.tab-body v]))

(defn combined
  "A merged view of the tab headers and the body of the currently selected tab.
  Takes tabs `state` of the form described in the docstring of this namespace."
  [state]
  [:<>
   [head state]
   [body state]])
