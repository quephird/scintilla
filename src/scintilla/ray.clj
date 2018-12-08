(ns scintilla.ray
  (:require [scintilla.camera :as c]
            [scintilla.matrix :as m]
            [scintilla.numeric :refer [ε]]
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
  (let [discriminant (- (* b b) (* 4 a c))]
    (cond
      (> 0 discriminant)
        []
      (zero? discriminant)
        [(/ b (* -2.0 a))]
      :else
        (let [√discriminant (Math/sqrt discriminant)]
          [(/ (+ b √discriminant) (* -2.0 a))
           (/ (- b √discriminant) (* -2.0 a))]))))

;; TODO: Change to intersections-for
(defmulti find-intersections
  "Takes an abritrary shape and a ray and returns a list
   of either zero, one, or two points of intersection, sorted
   by increasing value of t."
  (fn [shape _] (:shape-type shape)))

;; TODO: Need to put diagram below illustrating how and why this works.
(defmethod find-intersections :sphere
  [{:keys [matrix] :as shape} ray]
  (let [{:keys [point direction] :as local-ray} (transform ray (m/inverse matrix))
        shape-to-ray (u/subtract point [0 0 0 1.0])
        a            (u/dot-product direction direction)
        b            (* 2.0 (u/dot-product direction shape-to-ray))
        c            (- (u/dot-product shape-to-ray shape-to-ray) 1.0)
        tvals        (find-roots a b c)]
    (map #(make-intersection % shape) tvals)))

(defmethod find-intersections :plane
  [{:keys [matrix] :as shape} ray]
  (let [{:keys [point direction] :as local-ray} (transform ray (m/inverse matrix))
        [_ py _ _] point
        [_ dy _ _] direction]
     (if (> ε (Math/abs dy))
       []
       [(make-intersection (- (/ py dy)) shape)])))

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

(defn ray-for
  "Computes the ray for the given camera and (x,y) coordinates of its canvas,
   in terms of the coordinate system correspondent with the inverse
   of the camera's transform matrix."
  [{:keys [half-world-width half-world-height transform] :as camera} x y]
  (let [pixel-size (c/pixel-size-for camera)
        [offset-x offset-y] (map #(* (+ % 0.5) pixel-size) [x y])
        [world-x world-y]   (map - [half-world-width half-world-height] [offset-x offset-y])
        inverse-transform   (m/inverse transform)
        point'              (m/tuple-times inverse-transform [world-x world-y -1 1])
        origin'             (m/tuple-times inverse-transform [0 0 0 1])
        direction'          (u/normalize (u/subtract point' origin'))]
    (make-ray origin' direction')))

(defmulti local-normal-for (fn [shape _] (:shape-type shape)))

(defmethod local-normal-for :sphere
  [_ local-point]
  (u/subtract local-point [0.0 0.0 0.0 1.0]))

(defmethod local-normal-for :plane
  [_ _]
  [0 1 0 0])

(defn normal-for
  "This is the 'public' interface for computing the normal
   vector for any arbitrary type of shape. It first converts
   the world point to a local point, computes the normal in
   that coordinate system by deferring the specialized
   implementation for the shape, then transforms it back to the
   world coordinate system."
  [{:keys [matrix] :as shape} world-point]
  (let [local-normal (as-> matrix $
                           (m/inverse $)
                           (m/tuple-times $ world-point)
                           (local-normal-for shape $))]
    (-> matrix
        m/inverse
        m/transpose
        (m/tuple-times local-normal)
        (assoc 3 0)  ;; TODO: This is a hack per the book; look for better way
        u/normalize)))

(defn reflected-vector-for
  "Computes the vector that is the result of reflecting
   the in-vector around the normal vector."
  ;;
  ;;                    normal
  ;;                    vector
  ;;                г      ^       ⟋
  ;;                  ⟍    |    ⟋
  ;;  reflected vector  ⟍  |  ⟋ incident vector
  ;;                      ⟍|∟
  ;;                ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
  [in-vector normal-vector]
  (->> normal-vector
       (u/dot-product in-vector)
       (* 2.0)
       (u/scalar-times normal-vector)
       (u/subtract in-vector)))

(defn make-prepared-hit
  "Returns a map representing the object hit by the ray
   with other pre-computed entities associated with it."
  [hit ray]
  (let [surface-point    (position ray (:t hit))
        surface-normal   (normal-for (:shape hit) surface-point)
        eye-direction    (u/subtract (:direction ray))
        reflected-vector (reflected-vector-for (:direction ray) surface-normal)
        inside?          (> 0 (u/dot-product surface-normal eye-direction))]
    (assoc hit
      :surface-point    (u/plus surface-point (u/scalar-times surface-normal ε))
      :surface-normal   (if inside?
                          (u/subtract surface-normal)
                          surface-normal)
      :eye-direction    eye-direction
      :reflected-vector reflected-vector
      :inside           inside?)))
