(ns scintilla.csg
  (:require [scintilla.shapes :as s]))

(defn make-csg-shape
  [operation-type left-shape right-shape]
  {:operation-type operation-type
   :left-shape     left-shape
   :right-shape    right-shape})

(defn make-union
  [left-shape right-shape]
  (make-csg-shape :union left-shape right-shape))
  
(defn intersection-allowed?
  [operation-type left-shape-hit? inside-left-shape? inside-right-shape?]
  (case operation-type
    :union
    ;;                            ______  ______ 
    ;;                          ⟋       ⟋⟍       ⟍ 
    ;;                         /       /  \        \
    ;;             -----------✅------|----|-------✅-------
    ;;                         \       \  /        /
    ;;                          ⟍       ⟍⟋       ⟋ 
    ;;                            ‾‾‾‾‾‾  ‾‾‾‾‾‾
    ;; We only want to count intersections that either hit the left shape
    ;; but _not_ from within the other, _or_ hit the right shape and not from
    ;; within it
      (if (or (and left-shape-hit? (not inside-right-shape?))
              (and (not left-shape-hit?) (not inside-left-shape?)))
        true
        false)
    :else
      false))
