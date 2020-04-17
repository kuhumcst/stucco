(ns kuhumcst.recap.layout.core
  "Reagent components to make page layouts from top-level Landmark Regions.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#aria_landmark")

(defn- assert-single-element
  "Throw exception if a specified top-level landmark has multiple instances.
  Not all landmarks allow multiple instances."
  [landmark [tag & _ :as data]]
  (when (and data
             (not (or (keyword? tag)
                      (fn? tag))))
    (throw (ex-info (str landmark " is not a single element.") data))))

(defn generic
  "Generic page layout for the top-level `landmarks`."
  [{:keys [banner
           complementary
           contentinfo
           main]
    :as   landmarks}]
  (assert-single-element "banner" banner)
  (assert-single-element "main" main)
  [:<>
   banner
   main
   complementary
   contentinfo])
