(ns dk.cst.stucco.dom.keyboard
  "Helpers for ARIA-compliant keyboard navigation.

  ARIA reference:
    https://www.w3.org/TR/wai-aria-practices-1.1/#keyboard"
  (:require [clojure.set :as set]
            [dk.cst.stucco.dom.focus :as focus]))

;; https://javascript.info/bubbling-and-capturing
;; https://www.mutuallyhuman.com/blog/keydown-is-the-only-keyboard-event-we-need/

(defn- role-siblings
  "Get siblings for an `element` (including itself) with the same ARIA role."
  [element]
  (let [role (.getAttribute element "role")]
    (->> (.-children (.-parentNode element))
         (filter #(= role (.getAttribute % "role"))))))

(defn- adjacent-role-siblings
  "Get adjacent role siblings for an `element` as [before after]."
  [element]
  (loop [before []
         after  nil
         [sibling & siblings] (role-siblings element)]
    (cond
      (nil? sibling) [before after]
      (= element sibling) (recur before [] siblings)
      after (recur before (conj after sibling) siblings)
      before (recur (conj before sibling) after siblings))))

;; https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key
(def spacebar
  #{" " "Spacebar"})

(def enter
  #{"Enter"})

(def up
  #{"ArrowUp" "Up"})

(def down
  #{"ArrowDown" "Down"})

(def left
  #{"ArrowLeft" "Left"})

(def right
  #{"ArrowRight" "Right"})

(def prev-item
  (set/union up left))

(def next-item
  (set/union down right))

(def click-item
  (set/union spacebar enter))

(def supported-keys
  (set/union prev-item next-item click-item))

(defn roving-tabindex-handler
  "OnKeyDown handler with keyboard functionality needed for a roving tabindex.
  Focus moves between adjacent DOM siblings with the same ARIA role.

  Requires :on-click and :role to have been set on the affected elements.
  The elements should also update their :tab-index attribute reactively.

  ARIA references:
    https://www.w3.org/TR/wai-aria-practices-1.1/#kbd_general_within"
  [e]
  (when (supported-keys e.key)
    (.preventDefault e)
    (.stopPropagation e)
    (let [[before after] (adjacent-role-siblings e.target)]
      (condp contains? e.key
        click-item (do
                     ;; Focus is both set directly and requested asynchronously.
                     ;; Which method is effective is determined by whether the
                     ;; element has to be re-rendered (async) or not (direct).
                     (focus/request! e.target.id)
                     (.click e.target)
                     (.focus e.target))
        prev-item (.focus (last (if (empty? before)
                                  after
                                  before)))
        next-item (.focus (first (if (empty? after)
                                   before
                                   after)))
        :no-op))))
