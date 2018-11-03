(ns scintilla.shapes
  (:require [scintilla.matrix :refer [I₄]]))

(defn make-sphere
  ([center]
    (make-sphere center I₄))
  ([center matrix]
    {:shape-type :sphere
     :shape-center center
     :matrix matrix}))
