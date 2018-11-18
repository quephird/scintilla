(ns scintilla.ray
  (:require [scintilla.lighting :as l]
            [scintilla.matrix :as m]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]
            [scintilla.tuple :as u]))

(defn make-ray
  "Constructs a data structure representing a ray"
  [point direction]
  {:point point
   :direction direction})

(defn position
  "Computes the position along the given ray parameterized by t."
  [{:keys [point direction] :as ray} t]
  (u/plus point (u/scalar-times direction t)))

;; TODO: Think of how to either move this into the transformation
;; namespace or move what's currently in there back into the matrix
;; namespace.
(defn transform
  "NOTA BENE: note that a translation matrix applied to a vector
   is an effective no-op, but that a scaling matrix is not, which
   works to our advantage here because that is the desired behavior
   and we don't need to know what kind of transformation matrix
   is passed in."
  [{:keys [point direction]} matrix]
  (make-ray (m/tuple-times matrix point) (m/tuple-times matrix direction)))

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

(defmulti find-intersections
  "Takes an abritrary shape and a ray and returns a list
   of either zero, one, or two points of intersection, sorted
   by increasing value of t."
  (fn [shape _] (:shape-type shape)))

(defmethod find-intersections :sphere
  [{:keys [matrix] :as shape} ray]
  (let [{:keys [point direction]} (transform ray (m/inverse matrix))
        shape-to-ray (u/subtract point [0 0 0 1.0])
        a            (u/dot-product direction direction)
        b            (* 2.0 (u/dot-product direction shape-to-ray))
        c            (- (u/dot-product shape-to-ray shape-to-ray) 1.0)
        tvals        (find-roots a b c)]
    (map #(make-intersection % shape) tvals)))

(defn find-all-intersections
  "Returns the set of all intersections that the given ray
   makes with the set of objects in the given world."
  [{:keys [objects] :as world} ray]
  (apply concat (map #(find-intersections % ray) objects)))

(defn find-hit
  "Takes a set of intersections and selects only the
   'visible' one, which is the intesection with the least positive
   t value out of the set."
   [intersections]
   (->> intersections
        (sort-by :t)
        (some (fn [i] (if (< 0 (:t i)) i)))))

(defn make-prepared-hit
  "Returns a map representing the object hit by the ray
   with other pre-computed entities associated with it."
  [hit ray]
  (let [material       (get-in hit [:shape :material])
        surface-point  (position ray (:t hit))
        surface-normal (s/find-normal (:shape hit) surface-point)
        eye-direction  (u/subtract (:direction ray))
        inside         (> 0 (u/dot-product surface-normal eye-direction))]
    (assoc hit
      :surface-point  surface-point
      :surface-normal (if inside
                          (u/subtract surface-normal)
                          surface-normal)
      :eye-direction  eye-direction
      :inside         inside)))

(defn color-for
  "For the given world and ray from the camera to the canvas,
   return the color correspondent to the hit object or simply
   black if no object is hit."
  [{:keys [light] :as world} ray]
  (let [hit (-> world
                (find-all-intersections ray)
                (find-hit))]
    (if (nil? hit)
      [0 0 0]
      (as-> hit $
            (make-prepared-hit $ ray)
            (l/lighting light $)))))
