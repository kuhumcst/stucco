(ns kuhumcst.recap.util)

(defn vec-dissoc
  [coll n]
  (vec (concat (subvec coll 0 n)
               (subvec coll (inc n)))))

(defn vec-assoc
  [coll n v]
  (let [[before after] (split-at n coll)]
    (vec (concat before [v] after))))

;; TODO: rewrite or re-evaluate existence?
;; For keyboard selection and accessibility in general.
(defn- tab-attr
  "Get attr map for element that should be made tabbable based on `on-click`.
  This map should be merged with the attr of all components that need this kind
  of a uniform tab behaviour (... unless they can be tabbed to by default)."
  [on-click]
  {:tab-index   0
   :on-key-down (fn [e]
                  (when (or (= 13 (.-keyCode e))            ; enter
                            (= 32 (.-keyCode e)))           ; spacebar
                    (.preventDefault e)
                    (on-click)))
   :on-click    (fn [e]
                  (.preventDefault e)
                  (on-click))})
