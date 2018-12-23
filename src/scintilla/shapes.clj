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

(defn make-cube
  "The default cube is centered at the world origin
   and has half-length of 1."
  [& args]
  (apply make-shape :cube args))

(defn make-plane
  "The default plane lies in the 𝑥𝑧 plane."
  [& args]
  (apply make-shape :plane args))

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

(defmulti intersections-for
  "Takes an abritrary shape and a ray and returns a list
   of either zero, one, or two points of intersection, sorted
   by increasing value of t."
  (fn [shape _] (:shape-type shape)))

;; TODO: Need to put diagram below illustrating how and why this works.
(defmethod intersections-for :sphere
  [{:keys [matrix] :as shape} ray]
  (let [{:keys [point direction] :as local-ray} (r/transform ray (m/inverse matrix))
        shape-to-ray (u/subtract point [0 0 0 1])
        a            (u/dot-product direction direction)
        b            (* 2.0 (u/dot-product direction shape-to-ray))
        c            (- (u/dot-product shape-to-ray shape-to-ray) 1.0)
        tvals        (quadratic-roots-for a b c)]
    (map #(make-intersection % shape) tvals)))

(defmethod intersections-for :plane
  [{:keys [matrix] :as shape} ray]
  (let [{:keys [point direction] :as local-ray} (r/transform ray (m/inverse matrix))
        [_ py _ _] point
        [_ dy _ _] direction]
     (if (> ε (Math/abs dy))
       []
       [(make-intersection (- (/ py dy)) shape)])))

(defn- check-axis
  "Helper function for computing minimum and maximum
   values for t for each of the x, y, and z components of the
   intersecting ray"
  [pointᵢ directionᵢ]
  ;; This is done to avoid division-by-zero errors
  (if (>= (Math/abs directionᵢ) ε)
    (let [t₁ (/ (- -1.0 pointᵢ) directionᵢ)
          t₂ (/ (- 1.0 pointᵢ) directionᵢ)]
      [(min t₁ t₂) (max t₁ t₂)])
    (let [t₁ (* (- -1.0 pointᵢ) Double/MAX_VALUE)
          t₂ (* (- 1.0 pointᵢ) Double/MAX_VALUE)]
      [(min t₁ t₂) (max t₁ t₂)])))

(defmethod intersections-for :cube
  ;; This uses a trick by computing the possible values of t
  ;; by examining each coordinate first. Note that the default
  ;; cube is always centered at the origin with all vertices one
  ;; unit away. Considering a 2D square first, which is highlighted
  ;; below, and a ray with its origin at (-3, -2) and direction (1,1),
  ;; we see the following: 
  ;;
  ;;              │    ┊    │⟋
  ;;              │    ┊   ⟋│
  ;;        (-1,1)│  i₂┊ ⟋  │(1,1)
  ;;       ───────╆━━━━⟋━━━━╅──────
  ;;              ┃ ⟋  ┊    ┃
  ;;       ┈┈┈┈┈┈┈⟋┈┈┈┈┼┈┈ ┈╂┈┈┈┈┈
  ;;            ⟋ ┃i₁  ┊    ┃
  ;;       ───⟋───╄━━━━┿━━━━╃──────
  ;;        ⟋ (-1,-1)  ┊    │(1,-1)
  ;;              │    ┊    │
  ;;              │    ┊    │
  ;;
  ;; From the algorithm below we see that the t values for each axis are:
  ;;
  ;; x                y
  ;; (-1 - -3)/1 = 2  (-1 - -2)/1 = 1
  ;; (1 - -3)/1 = 4   (1 - -2)/1  = 3
  ;; => ts = (2,4)     => ts = (1,3)
  ;;
  ;; Taking the max of the min's of each pair of ts we get 2,
  ;; and taking the min of the max's of each pair of ts we get 3,
  ;; which yields i₁ to be at (-3,-2) + 2*(1,1) = (-1,0),
  ;; and i₂ to be at (-3,-2) + 3*(1,1) = (0,1), which is exactly
  ;; what we expected!
  ;;
  [{:keys [matrix] :as shape} ray]
  (let [{:keys [point direction] :as local-ray} (r/transform ray (m/inverse matrix))
        [px py pz _] point
        [dx dy dz _] direction
        t-pairs      (map #(check-axis %1 %2) [px py pz] [dx dy dz])
        t-min        (apply max (map first t-pairs))
        t-max        (apply min (map second t-pairs))]
     (if (> t-min t-max)
       []
       (map #(make-intersection % shape) [t-min t-max]))))

;; TODO: Need diagrams for below
(defmulti local-normal-for (fn [shape _] (:shape-type shape)))

(defmethod local-normal-for :sphere
  [_ local-point]
  (u/subtract local-point [0.0 0.0 0.0 1.0]))

(defmethod local-normal-for :plane
  [_ _]
  [0 1 0 0])

(defmethod local-normal-for :cube
  [_ [x y z _ :as local-point]]
  (let [max-coordinate (->> [x y z]
                           (map #(Math/abs %))
                           (apply max))]
    (cond
      (= max-coordinate (Math/abs x))
        [x 0 0 0]
      (= max-coordinate (Math/abs y))
        [0 y 0 0]
      (= max-coordinate (Math/abs z))
        [0 0 z 0])))

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
