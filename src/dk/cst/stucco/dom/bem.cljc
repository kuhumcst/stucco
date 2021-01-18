(ns dk.cst.stucco.dom.bem
  (:require [clojure.string :as str]))

(def bem-block
  #"(\w-?)+")

;; Assumes BEM convention is respected, i.e. only a single block class applied.
(defn add-modifier!
  "Add a BEM `modifier` class to an `element`."
  [element modifier]
  (let [class-list (.-classList element)
        class      (str (->> (array-seq class-list)
                             (filter (partial re-matches bem-block))
                             (first))
                        "--" modifier)]
    (.add class-list class)))

(defn remove-modifier!
  "Remove a BEM `modifier` class from an `element`."
  [element modifier]
  (let [class-list (.-classList element)
        class      (->> (array-seq class-list)
                        (filter #(str/ends-with? % (str "--" modifier)))
                        (first))]
    (.remove class-list class)))