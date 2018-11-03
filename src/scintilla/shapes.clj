(ns scintilla.shapes
  (:require [scintilla.matrix :refer [I₄]]))

(defn make-sphere
  ([]
    (make-sphere I₄))
  ([matrix]
    {:shape-type :sphere
     :matrix matrix}))
