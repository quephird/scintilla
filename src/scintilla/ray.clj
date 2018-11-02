(ns scintilla.ray
  (:require [scintilla.matrix :as m]
            [scintilla.transformation :as t]
            [scintilla.tuple :refer :all]))

(defn make-ray
  "Constructs a data structure representing a ray"
  [point direction]
  {:point point
   :direction direction})

(defn position
  "Computes the position along the given ray parameterized by t."
  [{:keys [point direction] :as ray} t]
  (+ point (* direction t)))

(defn make-intersection
  "Constructs a data structure representing an intersection"
  [t shape]
  {:t t
   :shape shape})

(defn- find-roots
  "Helper function to determine the set of real roots to the quadratic equation:
   𝑎𝑥² + 𝑏𝑥 + 𝑐 = 𝟢"
  [a b c]
  (let [discriminant (clojure.core/- (clojure.core/* b b) (clojure.core/* 4 a c))]
    (cond
      (> 0 discriminant)
        []
      (zero? discriminant)
        [(clojure.core// b (clojure.core/* -2.0 a))]
      :else
        (let [√discriminant (Math/sqrt discriminant)]
          [(clojure.core// (clojure.core/+ b √discriminant) (clojure.core/* -2.0 a))
           (clojure.core// (clojure.core/- b √discriminant) (clojure.core/* -2.0 a))]))))

(defn find-intersections
  "Takes an abritrary shape and a ray and returns a list
   of either zero, one, or two points of intersection, sorted
   by increasing value of t."
  [{:keys [shape-center radius] :as shape}
   {:keys [point direction]}]
  (let [shape-to-ray (- point shape-center)
        a (⋅ direction direction)
        b (clojure.core/* 2 (⋅ direction shape-to-ray))
        c (clojure.core/- (⋅ shape-to-ray shape-to-ray) radius)
        tvals (find-roots a b c)]
    (map #(make-intersection % shape) tvals)))

(defn find-hit
  "Takes a set of intersections and selects only the
   'visible' one, which is the intesection with the least positive
   t value out of the set."
   [intersections]
   (->> intersections
       (sort-by :t)
       (some (fn [i] (if (< 0 (:t i)) i)))))

;; TODO: Think of how to either move this into the transformation
;; namespace or move what's currently in there back into the matrix
;; namespace.
(defn translate
  [{:keys [point direction]} x y z]
  (let [T (t/translation-matrix x y z)]
    (make-ray (m/tuple-times T point) direction)))

(defn scale
  [{:keys [point direction]} x y z]
  (let [S (t/scaling-matrix x y z)]
    (make-ray (m/tuple-times S point) (m/tuple-times S direction))))
