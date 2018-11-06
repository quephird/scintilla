(ns scintilla.shapes
  (:require [scintilla.matrix :refer [I₄]]
            [scintilla.tuple :refer :all]))

(defn make-sphere
  ([color]
    (make-sphere color I₄))
  ([color matrix]
    {:shape-type :sphere
     :matrix matrix
     :color color}))

(defmulti find-normal (fn [shape _] (:shape-type shape)))

(defmethod find-normal :sphere
  [shape point]
  (normalize (- point [0.0 0.0 0.0 1.0])))
