(ns kuhumcst.recap.layout.landmarks
  "Reagent components for populating ARIA Landmark Regions.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#aria_landmark")

(defn- assert-label
  "Throw exception if a landmark is unlabeled. Labeling is required for
  landmarks that allow multiple instances."
  [landmark {:keys [aria-label aria-labelledby] :as attr}]
  (when (not (or aria-label aria-labelledby))
    (throw (ex-info (str landmark " landmark is unlabeled.") attr))))

(defn banner
  "Identifies site-oriented content at the beginning of a document, i.e. things
  such as the logo and site-specific search tool.

  NOTE: banner is a top-level landmark."
  [& content]
  (into [:header] content))

(defn complementary
  "Supporting section of the document, designed to be complementary to the main
  content, but remaining meaningful when separated from the main content.

  NOTE: complementary is a top-level landmark."
  [& [attr :as content]]
  (assert-label "complementary" attr)
  (into [:aside] content))

(defn contentinfo
  "Identifies common information at the bottom of each page within a website,
  such as copyrights and links to privacy and accessibility statements.

  NOTE: complementary is a top-level landmark."
  [& content]
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
  [& content]
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
