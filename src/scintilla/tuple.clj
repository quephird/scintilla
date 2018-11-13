(ns scintilla.tuple
  (:require [scintilla.numeric :refer [≈]]))

(defn point?
  [[_ _ _ w]]
  (= 1 w))

(defn vector?
  [[_ _ _ w]]
  (= 0 w))

(defn make-point
  [x y z]
  [x y z 1])

(defn make-vector
  [x y z]
  [x y z 0])

;; TODO: Consider using specs to enforce various things here

; Defines a protocol to allow colors to use the standard
; mathematical operators.
(defprotocol Tuple
  (+ [v1 v2])
  (- [v] [v1 v2])
  (⋅ [v1 v2])
  (⨯ [v1 v2])
  (* [v s])
  (/ [v s])
  (magnitude [v])
  (normalize [v]))

;; TODO: Need to come up with better strategy for throwing
;; under certain conditions
(extend-type clojure.lang.PersistentVector
  Tuple
  (+ [[_ _ _ w1 :as v1] [_ _ _ w2 :as v2]]
    (if (or (zero? w1) (zero? w2))
      (mapv clojure.core/+ v1 v2)
      (throw (Exception. "Cannot add two points."))))
  (- ([v] (mapv clojure.core/- v))
   ([[_ _ _ w1 :as v1] [_ _ _ w2 :as v2]]
    (if (not (and (zero? w1) (≈ w2 1.0)))
      (mapv clojure.core/- v1 v2)
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
    (if (zero? w)
      [(clojure.core/* s x) (clojure.core/* s y) (clojure.core/* s z) w]
      (throw (Exception. "Cannot scale a point."))))
  (/ [[_ _ _ w :as v] s]
    (if (zero? w)
      (* v (clojure.core// 1.0 s))
      (throw (Exception. "Cannot scale a point."))))
  (magnitude [[x y z w]]
    (if (zero? w)
      (->> [x y z]
           (map #(Math/pow % 2))
           (reduce clojure.core/+ 0)
           (Math/sqrt))
      (throw (Exception. "Cannot scale a point."))))
  (normalize [[_ _ _ w :as v]]
    (if (zero? w)
      (/ v (magnitude v))
      (throw (Exception. "Cannot normalize a point.")))))
