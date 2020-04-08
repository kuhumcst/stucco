(ns kuhumcst.recap.tab
  "Tab components that rely entirely on external, derefable state, e.g. a ratom.
  The `state` should always deref as a map with the following required keys:

    :kvs - key-value pairs of tab headers and bodies.

  And optionally:

    :i - the index of the currently selected tab.

  The tab `head` and `body` are - in principle - decoupled. However, a merged
  representation is available through the `window` component.")

(defn- label
  "The header in the tab `state` at index `n`."
  [n state]
  (let [{:keys [i kvs] :or {i 0}} @state
        [k v] (nth kvs n)
        id (random-uuid)]
    [:<> {:key id}                                          ; make checked work
     [:input {:id        id
              :type      "radio"
              :checked   (= n i)
              :read-only true
              :value     n}]
     [:label {:for id} k]]))

(defn head
  "The headers available in the tab `state`."
  [state]
  (let [{:keys [kvs]} @state
        change-label (fn [e]
                       (let [i (js/parseInt (.. e -target -value))]
                         (swap! state assoc :i i)))]
    [:form.tab-head {:on-change change-label}
     (for [n (range (count kvs))]
       ^{:key n} [label n state])
     [:span.tab-head__spacer]]))

(defn body
  "The currently selected body in the tab `state`."
  [state]
  (let [{:keys [i kvs] :or {i 0}} @state
        [_ v] (nth kvs i)]
    [:article.tab-body v]))

(defn window
  "A merged view of the tab headers and the body of the currently selected tab.
  Takes tab `state` in the form described in the description of this namespace."
  [state]
  [:<>
   [head state]
   [body state]])
