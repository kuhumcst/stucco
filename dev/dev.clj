(ns dev
  (:require [shadow.cljs.devtools.api :as shadow]))

(defn start
  []
  (shadow/watch :dev)
  (shadow/repl :dev))
