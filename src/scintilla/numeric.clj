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

(defprotocol Tuple
  (+ [v1 v2])
  (- [v] [v1 v2])
  (* [v s])
  (/ [v s]))

(extend-type clojure.lang.PersistentVector
  Tuple
  (+ [[_ _ _ w1 :as v1] [_ _ _ w2 :as v2]]
      (case [w1 w2]
        ([1 0] [0 1] [0 0]) (mapv clojure.core/+ v1 v2)
        (throw (Exception. "Cannot add two points."))))
  (- ([v] (mapv clojure.core/- v))
     ([[_ _ _ w1 :as v1] [_ _ _ w2 :as v2]]
      (case [w1 w2]
        ([1 1] [1 0] [0 0]) (mapv clojure.core/- v1 v2)
        (throw (Exception. "Cannot subtract a point from a vector.")))))
  (* [[x y z w] s]
      [(clojure.core/* s x) (clojure.core/* s y) (clojure.core/* s z) w])
  (/ [v s]
     (* v (clojure.core// 1.0 s))))
