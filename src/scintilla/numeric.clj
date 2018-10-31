(ns scintilla.numeric
  (:refer-clojure :exclude [+ - * /]))

; Maximum tolerance for near equality.
(def *epsilon* 0.00001)

; Sneaky, and perhaps too cute, way of defining a new
; operator for use with both numeric types and vectors.
(defprotocol AlmostEqual
  (≈ [x y]))

; Base implementation for handling Doubles.
(extend java.lang.Double
  AlmostEqual
  {:≈ (fn [d1 d2]
        (< (Math/abs (clojure.core/- d1 d2)) *epsilon*))})

; Casts Longs as Doubles and calls the above implementation.
(extend java.lang.Long
  AlmostEqual
  {:≈ (fn [l1 l2]
        (≈ (double l1) (double l2)))})

; Compares each pair of elements from the two vectors.
(extend clojure.lang.PersistentVector
  AlmostEqual
  {:≈ (fn [v1 v2]
        (every? true? (map ≈ v1 v2)))})

; Compares each pair of elements from the two sets.
(extend clojure.lang.PersistentHashSet
  AlmostEqual
  {:≈ (fn [v1 v2]
        (every? true? (map ≈ v1 v2)))})
