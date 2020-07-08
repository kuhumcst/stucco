(ns recap.util)

(defn vec-dissoc
  [coll n]
  (vec (concat (subvec coll 0 n)
               (subvec coll (inc n)))))

(defn vec-assoc
  [coll n v]
  (let [[before after] (split-at n coll)]
    (vec (concat before [v] after))))
