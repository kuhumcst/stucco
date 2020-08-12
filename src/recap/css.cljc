(ns recap.css
  "CSS content available programmatically for use in the Shadow DOM."
  (:require [shadow.resource :as resource]
            [clojure.string :as str]))

(def resources
  {:carousel (resource/inline "public/css/carousel.css")
   :lens     (resource/inline "public/css/lens.css")
   :tabs     (resource/inline "public/css/tabs.css")
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
