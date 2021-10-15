(ns dk.cst.stucco.util.css
  "CSS content available programmatically for use in the Shadow DOM."
  (:require [shadow.resource :as resource]
            [clojure.string :as str]))

(def resources
  {:document (resource/inline "public/css/document.css")
   :group    (resource/inline "public/css/group.css")
   :pattern  (resource/inline "public/css/pattern.css")
   :landmark (resource/inline "public/css/landmark.css")
   :shared   (resource/inline "public/css/shared.css")})

(def default-theme
  (resource/inline "public/css/theme.css"))

(def shadow-style
  "The combined CSS content - including the default theme - for all widgets."
  (let [titles (->> (keys resources)
                    (map #(str "\n\n/*\n\t === " (name %) ".css ===\n*/\n")))
        theme  (str/replace-first default-theme ":root" ":host")]
    (->> (vals resources)
         (interleave titles)
         (apply str "/*\n\t === theme.css ===\n*/\n" theme))))
