(ns scintilla.shapes
  (:require [scintilla.materials :as a]
            [scintilla.matrix :refer [I₄] :as m]
            [scintilla.numeric :refer [ε]]
            [scintilla.patterns :as p]
            [scintilla.ray :as r]
            [scintilla.tuple :as u]))

;; We need a way to uniquely identify shapes, but since
;; we are using raw maps and not instantiating and referengin
;; objects, we need an explicit strategy, and so we use UUIDs.
(defn make-shape
  ([shape-type]
    (make-shape shape-type a/default-material))
  ([shape-type material]
    (make-shape shape-type material I₄))
  ([shape-type material transform]
   {:id         (java.util.UUID/randomUUID)
    :shape-type shape-type
    :material   material
    :matrix     transform}))

(defn make-sphere
  "The default sphere is centered at the world origin
   and has radius 1."
  [& args]
  (apply make-shape :sphere args))

(defn make-plane
  "The default plane lies in the 𝑥𝑧 plane."
  [& args]
  (apply make-shape :plane args))

(defn color-for
  "This function either returns the simple color for the
   entire hit shape or defers computation of the color to the
   shape's pattern implementation itself if it exists
   for the surface point in question."
  [prepared-hit]
  (let [{:keys [pattern color]} (get-in prepared-hit [:shape :material])]
    (if (nil? pattern)
      color
      (p/color-for prepared-hit))))

(defn- quadratic-roots-for
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

(defn make-intersection
  "Constructs a data structure representing an intersection"
  [t shape]
  {:t t
   :shape shape})

;; TODO: Change to intersections-for
(defmulti find-intersections
  "Takes an abritrary shape and a ray and returns a list
   of either zero, one, or two points of intersection, sorted
   by increasing value of t."
  (fn [shape _] (:shape-type shape)))

;; TODO: Need to put diagram below illustrating how and why this works.
(defmethod find-intersections :sphere
  [{:keys [matrix] :as shape} ray]
  (let [{:keys [point direction] :as local-ray} (r/transform ray (m/inverse matrix))
        shape-to-ray (u/subtract point [0 0 0 1])
        a            (u/dot-product direction direction)
        b            (* 2.0 (u/dot-product direction shape-to-ray))
        c            (- (u/dot-product shape-to-ray shape-to-ray) 1.0)
        tvals        (quadratic-roots-for a b c)]
    (map #(make-intersection % shape) tvals)))

(defmethod find-intersections :plane
  [{:keys [matrix] :as shape} ray]
  (let [{:keys [point direction] :as local-ray} (r/transform ray (m/inverse matrix))
        [_ py _ _] point
        [_ dy _ _] direction]
     (if (> ε (Math/abs dy))
       []
       [(make-intersection (- (/ py dy)) shape)])))

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
