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
  (⋅ [v1 v2])
  (⨯ [v1 v2])
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
  (⋅ [[x1 y1 z1 w1] [x2 y2 z2 w2]]
    (if (and (zero? w1) (zero? w2))
      (clojure.core/+ (clojure.core/* x1 x2) (clojure.core/* y1 y2) (clojure.core/* z1 z2))
      (throw (Exception. "Can only take dot product of two vectors"))))
  (⨯ [[x1 y1 z1 w1] [x2 y2 z2 w2]]
    (if (and (zero? w1) (zero? w2))
      [(clojure.core/- (clojure.core/* y1 z2) (clojure.core/* z1 y2))
       (clojure.core/- (clojure.core/* z1 x2) (clojure.core/* x1 z2))
       (clojure.core/- (clojure.core/* x1 y2) (clojure.core/* y1 x2))
       0]
      (throw (Exception. "Can only take cross product of two vectors"))))
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
