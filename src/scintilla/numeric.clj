(ns scintilla.numeric)

(def π 3.1415926536)
(def π⟋2 (/ π 2.0))
(def π⟋3 (/ π 3.0))
(def π⟋4 (/ π 4.0))
(def π⟋6 (/ π 6.0))

; Maximum tolerance for near equality.
(def ε 0.0001)

; Sneaky, and perhaps too cute, way of defining a new
; operator for use with both numeric types and vectors.
(defprotocol AlmostEqual
  (≈ [x y]))

; Base implementation for handling Doubles.
(extend java.lang.Double
  AlmostEqual
  {:≈ (fn [d1 d2]
        (< (Math/abs (clojure.core/- d1 d2)) ε))})

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
