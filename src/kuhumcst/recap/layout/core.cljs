(ns kuhumcst.recap.layout.core
  "Reagent components to make page layouts from top-level Landmark Regions.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#aria_landmark")

;; Some landmarks only allow single instances, e.g. banner or main.
(defn- assert-single-element
  "Assert from hiccup `data` that the `landmark-type` is a single instance."
  [landmark-type [tag & _ :as data]]
  (assert (or (and (keyword? tag)
                   (not= :<> tag))
              (fn? tag))
          (str landmark-type " is not a single element: " data)))

(defn root
  "Root layout comprised of the top-level `landmarks`."
  [{:keys [banner
           complementary
           content-info
           main]
    :as   landmarks}]
  (assert-single-element "banner" banner)
  (assert-single-element "main" main)
  [:<>
   banner
   main
   complementary
   content-info])
