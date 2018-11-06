(ns scintilla.shapes
  (:require [scintilla.matrix :refer [I₄]]))

(defn make-sphere
  ([color]
    (make-sphere color I₄))
  ([color matrix]
    {:shape-type :sphere
     :matrix matrix
     :color color}))
