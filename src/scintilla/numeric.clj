(ns scintilla.numeric
  (:refer-clojure :exclude [+ - * /]))

(def *epsilon* 0.00001)

(defprotocol AlmostEqual
  (≈ [x y]))

(extend java.lang.Double
  AlmostEqual
  {:≈ (fn [d1 d2]
        (< (Math/abs (clojure.core/- d1 d2)) *epsilon*))})

(extend java.lang.Long
  AlmostEqual
  {:≈ (fn [l1 l2]
        (≈ (double l1) (double l2)))})

(extend clojure.lang.PersistentVector
  AlmostEqual
  {:≈ (fn [v1 v2]
        (every? true? (map ≈ v1 v2)))})
