(ns scintilla.tuple
  (:require [scintilla.numeric :refer [≈]]))

(defn plus
  ([[_ _ _ w1 :as v1] [_ _ _ w2 :as v2]]
   (if (or (zero? w1) (zero? w2))
     (mapv clojure.core/+ v1 v2)
     (throw (Exception. "Cannot add two points."))))
  ([v1 v2 & vs]
   (plus v1 (apply plus v2 vs))))

(defn subtract
  ([v]
    (mapv - v))
  ([[_ _ _ w1 :as v1] [_ _ _ w2 :as v2]]
    (if (not (and (zero? w1) (≈ w2 1.0)))
      (mapv - v1 v2)
      (throw (Exception. "Cannot subtract a point from a vector.")))))

(defn dot-product
  [[x1 y1 z1 w1] [x2 y2 z2 w2]]
  (if (and (zero? w1) (zero? w2))
    (+ (* x1 x2) (* y1 y2) (* z1 z2))
    (throw (Exception. "Can only take dot product of two vectors"))))

(defn cross-product
  [[x1 y1 z1 w1] [x2 y2 z2 w2]]
  (if (and (zero? w1) (zero? w2))
    [(- (* y1 z2) (* z1 y2))
     (- (* z1 x2) (* x1 z2))
     (- (* x1 y2) (* y1 x2))
     0]
    (throw (Exception. "Can only take cross product of two vectors"))))

(defn scalar-times
  [[x y z w] s]
  (if (zero? w)
    [(* s x) (* s y) (* s z) w]
    (throw (Exception. "Cannot scale a point."))))

(defn scalar-divide
  [[_ _ _ w :as v] s]
  (if (zero? w)
    (scalar-times v (/ 1.0 s))
    (throw (Exception. "Cannot scale a point."))))

(defn magnitude
  [[x y z w]]
  (if (zero? w)
    (->> [x y z]
         (map #(Math/pow % 2))
         (reduce clojure.core/+ 0)
         (Math/sqrt))
    (throw (Exception. "Cannot scale a point."))))

(defn normalize
  [[_ _ _ w :as v]]
  (if (zero? w)
    (scalar-divide v (magnitude v))
    (throw (Exception. "Cannot normalize a point."))))
