(ns scintilla.numeric)

(def *epsilon* 0.00001)

(defprotocol AlmostEqual
  (≈ [x y]))

(extend java.lang.Double
  AlmostEqual
  {:≈ (fn [d1 d2]
        (< (Math/abs (- d1 d2)) *epsilon*))})

(extend java.lang.Long
  AlmostEqual
  {:≈ (fn [l1 l2]
        (≈ (double l1) (double l2)))})

(extend clojure.lang.PersistentVector
  AlmostEqual
  {:≈ (fn [v1 v2]
        (every? true? (map ≈ v1 v2)))})

(defn plus
  [[_ _ _ w1 :as v1] [_ _ _ w2 :as v2]]
  (if (or (not= 4 (count v1)) (not= 4 (count v2)))
    (throw (Exception. "Both arguments must be tuples of length four."))
    (case [w1 w2]
      ([1 0] [0 1] [0 0]) (map + v1 v2)
      (throw (Exception. "Cannot add two points.")))))

(defn minus
  [[_ _ _ w1 :as v1] [_ _ _ w2 :as v2]]
  (if (or (not= 4 (count v1)) (not= 4 (count v2)))
    (throw (Exception. "Both arguments must be tuples of length four."))
    (case [w1 w2]
      ([1 1] [1 0] [0 0]) (map - v1 v2)
      (throw (Exception. "Cannot subtract a point from a vector.")))))
