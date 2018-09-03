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
  (/ [v s])
  (magnitude [v])
  (normalize [v]))

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
    (if (= w 0)
      [(clojure.core/* s x) (clojure.core/* s y) (clojure.core/* s z) w]
      (throw (Exception. "Cannot scale a point."))))
  (/ [[_ _ _ w :as v] s]
    (if (= w 0)
      (* v (clojure.core// 1.0 s))
      (throw (Exception. "Cannot scale a point."))))
  (magnitude [[x y z w]]
    (if (= w 0)
      (->> [x y z]
           (map #(Math/pow % 2))
           (reduce clojure.core/+ 0)
           (Math/sqrt))
      (throw (Exception. "Cannot scale a point."))))
  (normalize [[_ _ _ w :as v]]
    (if (= w 0)
      (/ v (magnitude v))
      (throw (Exception. "Cannot normalize a point.")))))
