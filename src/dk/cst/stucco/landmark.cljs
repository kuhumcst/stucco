(ns dk.cst.stucco.landmark
  "Reagent components for composing page structure from WAI-ARIA Landmarks.

  These components should be always be used as top-level container elements for
  accessible web page layouts, since they help ensure that the page regions can
  be navigated in the correct order.

  Lower-level component groupings can be found in 'dk.cst.stucco.pattern' and
  'dk.cst.stucco.group'.

  ARIA references:
    https://www.w3.org/TR/wai-aria-practices-1.1/#aria_landmark
    https://www.w3.org/TR/wai-aria-practices-1.1/examples/landmarks/")

;; Some landmarks only allow single instances, e.g. banner or main.
(defn- assert-single-element
  "Assert from the `hiccup` that the `landmark-type` is a single instance."
  [landmark-type [tag & _ :as hiccup]]
  (assert (or (and (keyword? tag)
                   (not= :<> tag))
              (fn? tag))
          (str landmark-type " is not a single element: " hiccup)))

;; Required for landmarks that allow multiple instances.
(defn- assert-label
  "Assert from the `attr` that the `landmark-type` is labeled."
  [landmark-type {:keys [aria-label aria-labelledby] :as attr}]
  (assert (or aria-label aria-labelledby)
          (str landmark-type " landmark-type is unlabeled: " attr)))

(defn banner
  "Identifies site-oriented content at the beginning of a document, i.e. things
  such as the logo and site-specific search tool.

  NOTE: banner is a top-level landmark."
  [& [attr :as content]]
  (into [:header] content))

(defn complementary
  "Supporting section of the document, designed to be complementary to the main
  content, but remaining meaningful when separated from the main content.

  NOTE: complementary is a top-level landmark."
  [& [attr :as content]]
  (assert-label "complementary" attr)
  (into [:aside] content))

(defn content-info
  "Identifies common information at the bottom of each page within a website,
  such as copyrights and links to privacy and accessibility statements.

  NOTE: complementary is a top-level landmark."
  [& [attr :as content]]
  (into [:footer] content))

(defn form
  "Identifies a region that contains a collection of elements that combine to
  create a form when no other landmark is appropriate, e.g. main or search."
  [& [attr :as content]]
  (assert-label "form" attr)
  (into [:form] content))

(defn main
  "Identifies the primary content of the page.

  NOTE: main is a top-level landmark."
  [& [attr :as content]]
  (into [:main] content))

(defn navigation
  "Provides a way to identify groups of links that are intended to be used for
  website or page content navigation."
  [& [attr :as content]]
  (assert-label "navigation" attr)
  (into [:nav] content))

(defn region
  "Perceivable section of the page containing content that is sufficiently
  important for users to be able to navigate to the section."
  [& [attr :as content]]
  (assert-label "region" attr)
  (into [:section] content))

(defn search
  "Perceivable section of the page containing content that is sufficiently
  important for users to be able to navigate to the section."
  [& [attr :as content]]
  (assert-label "search" attr)
  (into [:form (assoc attr :role "search")] (rest content)))

;; TODO: optional skip-link? -- https://www.youtube.com/watch?v=cOmehxAU_4s
(defn root
  "Root layout comprised of the top-level `landmarks`."
  [{:keys [banner
           complementary
           content-info
           main]
    :as   landmarks}]
  (assert-single-element "banner" banner)
  (assert-single-element "main" main)
  ;; TODO: should probably be a container div to allow for styling?
  ;; Maybe use holy grail as default? https://youtu.be/qm0IfG1GyZU?t=465
  ;; Example: https://www.w3.org/TR/wai-aria-practices-1.1/examples/landmarks/
  [:<>
   banner
   main
   complementary
   content-info])
