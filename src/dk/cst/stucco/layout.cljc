(ns dk.cst.stucco.layout
  "Reagent components to compose page layouts from top-level Landmark Regions
  and custom sub-level groupings (e.g. combination).

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#aria_landmark"
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [dk.cst.stucco.state :as state]))

(defn- redistribute
  "Redistribute `weights` such that the `delta` is subtracted from the weight at
  index `n` and added to the weight at index `m`."
  [weights m n delta]
  (assoc weights
    m (max 0 (+ (get weights m) delta))
    n (max 0 (- (get weights n) delta))))

;; TODO: less clunky css for separator (thin oval gradient?)
;; TODO: check that (count weights) matches (count vs) - in spec?
;; TODO: invisible overlay container for resize mouse handlers
(defn combination
  "A combination of `vs`, with the space optionally partitioned by `weights`.
  If no `weights` are specified, each v will initially take up equal size.
  The `vs` will typically be various functionally related Stucco components."
  [{:keys [vs weights]
    :as   state}]
  (r/with-let [state        (state/prepare ::state/vs+weights state)
               resize-state (r/atom nil)]
    (let [{:keys [vs weights]
           :or   {weights (mapv (constantly 1) (range (count vs)))}} @state
          resizing     @resize-state
          key-prefix   (hash vs)
          columns      (->> weights
                            (map #(str "minmax(min-content, " % "fr)"))
                            (interpose "var(--grid-16)")
                            (str/join " "))
          resize-begin (fn [m n]
                         (fn [e]
                           (let [elements (.. e -target -parentNode -children)
                                 widths   (for [elem (take-nth 2 elements)]
                                            (.-offsetWidth elem))]
                             (reset! resize-state {:widths (vec widths)
                                                   :m      m
                                                   :n      n
                                                   :x      (.-clientX e)}))))
          resize-move  (fn [e]
                         (when-let [{:keys [widths m n x]} @resize-state]
                           (let [x'       (.-clientX e)
                                 weights' (redistribute widths m n (- x' x))]
                             (swap! state assoc :weights weights'))))
          resize-end   #(reset! resize-state nil)]
      [:div.combination {:on-mouse-move  resize-move
                         :on-mouse-up    resize-end
                         :on-mouse-leave resize-end
                         :class          (when resizing
                                           "combination--resize")
                         :style          {:grid-template-columns columns}}
       (for [[n v] (map-indexed vector vs)
             :let [key (str key-prefix "-" (hash v) "-" n)]]
         [:<> {:key key}
          (when (> n 0)
            [:div.combination__separator
             {:class         (when (= n (:n resizing))
                               "combination__separator--resize")
              :on-mouse-down (resize-begin (dec n) n)}])
          [:div v]])])))

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
  [& content]
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
  [:<>
   banner
   main
   complementary
   content-info])
