(ns kuhumcst.recap.tab
  "Reagent components for creating a tabbed UI.

  The tab components may share the following pieces of state:

    `kvs` - key-value pairs of tab headers and bodies.
    `i` - the index of the currently selected tab."
  (:require [kuhumcst.recap.state :refer [deref reset! swap!]])
  (:refer-clojure :exclude [deref reset! swap!]))

;; TODO: deterministic random background colour of the tab labels
;;       cycle through a set of standard colours and set as metadata on key-value pair
;; TODO: use internal atom if regular data structures are supplied?
;; TODO: drag and drop(zone) of tag labels, effectuating state changes
;; TODO: use grid rather than flexbox for facade?

(defn- label
  "The tab label available a index `n` of the tab `kvs` currently showing `i`."
  [n i kvs]
  (let [[k _] (nth kvs n)
        id (random-uuid)]
    [:<> {:key id}                                          ; make checked work
     [:input {:id        id
              :type      "radio"
              :checked   (= n i)
              :read-only true
              :value     n}]
     [:label {:for id}
      k]]))

(defn head
  "The headers available in the tab `opts`."
  [{:keys [kvs i] :as opts}]
  (let [*kvs         (deref kvs)
        *i           (deref i)
        change-label (fn [e]
                       (reset! i (js/parseInt (.. e -target -value))))]
    [:form.tab-head {:on-change change-label}
     (for [n (range (count *kvs))]
       ^{:key n} [label n *i *kvs])
     [:span.tab-head__spacer]]))

(defn body
  "The currently selected body in the tab `opts`."
  [{:keys [kvs i] :as opts}]
  (let [*kvs (deref kvs)
        *i   (deref i)
        [_ v] (nth *kvs *i)]
    [:article.tab-body v]))

(defn window
  "A merged view of the tab headers and the body of the currently selected tab.
  Takes tab `opts` in the form described in the description of this namespace."
  [{:keys [kvs i] :as opts}]
  [:<>
   [head opts]
   [body opts]])
