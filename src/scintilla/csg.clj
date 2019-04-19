(ns scintilla.csg
  (:require [scinilla.shapes :as s]))

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
      (if (or (and left-shape-hit? inside-right-shape?)
              (and (not left-shape-hit?) inside-left-shape?))
        true
        false)
    :else
      false))
