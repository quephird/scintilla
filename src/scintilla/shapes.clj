(ns scintilla.shapes
  (:require [scintilla.materials :as a]
            [scintilla.matrix :refer [I₄] :as m]
            [scintilla.numeric :refer [ε ≈]]
            [scintilla.patterns :as p]
            [scintilla.ray :as r]
            [scintilla.tuple :as u]))

(def default-options
  {:material        a/default-material
   :transform       I₄
   :minimum         (- Double/MAX_VALUE)
   :maximum         Double/MAX_VALUE
   :capped?         false})

(defn make-shape
  [shape-type options]
  (merge {:object-type :shape
          :shape-type shape-type}
         default-options
         options))

;; NOTA BENE: All shape constructors expect a map of options
;;            as their sole argument.
(defn make-sphere
  "The default sphere is centered at the world origin
   and has radius 1."
  [& options]
  (make-shape :sphere (into {} options)))

(defn make-cube
  "The default cube is centered at the world origin
   and has half-length of 1."
  [& options]
  (make-shape :cube (into {} options)))

(defn make-plane
  "The default plane lies in the 𝑥𝑧 plane."
  [& options]
  (make-shape :plane (into {} options)))

(defn make-cylinder
  "The default cylinder is centered at the world origin,
   has radius 1, and has infinite length along the y-axis."
  [& options]
  (make-shape :cylinder (into {} options)))

(defn make-cone
  "The default cylinder is centered at the world origin,
   has radius 1, and has infinite length along the y-axis."
  [& options]
  (make-shape :cone (into {} options)))

(defn make-triangle
  [p1 p2 p3 & options]
  (let [e1     (u/subtract p2 p1)
        e2     (u/subtract p3 p1)
        normal (u/normalize (u/cross-product e2 e1))
        triangle-options {:p1 p1 :p2 p2 :p3 p3
                          :e1 e1 :e2 e2
                          :normal normal}]
    (make-shape :triangle (apply merge triangle-options options))))

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
  [{:keys [transform] :as shape} ray]
  (let [{:keys [point direction] :as local-ray} (r/transform ray (m/inverse transform))
        shape-to-ray (u/subtract point [0 0 0 1])
        a            (u/dot-product direction direction)
        b            (* 2.0 (u/dot-product direction shape-to-ray))
        c            (- (u/dot-product shape-to-ray shape-to-ray) 1.0)
        tvals        (quadratic-roots-for a b c)]
    (map #(make-intersection % shape) tvals)))

(defmethod intersections-for :plane
  [{:keys [transform] :as shape} ray]
  (let [{:keys [point direction] :as local-ray} (r/transform ray (m/inverse transform))
        [_ py _ _] point
        [_ dy _ _] direction]
     (if (> ε (Math/abs dy))
       []
       [(make-intersection (- (/ py dy)) shape)])))

(defn- inside-cap?
  "This function is shared for both cones and cyinders,
   and determines whether the point at the end of the ray
   passed in, and after traveling distance t, is inside
   the cap with radius r."
  [ray t r]
  (let [[x _ z _] (r/position ray t)]
    (>= (* r r) (+ (* x x) (* z z)))))

(defn- intersections-for-cylinder-caps
  [{:keys [capped? minimum maximum] :as cylinder}
   {[px py pz _] :point [dx dy dz _] :direction :as ray}]
  (if (or (not capped?) (≈ 0.0 dy))
    []
    (->> [minimum maximum]
         (map #(/ (- % py) dy))
         (filter #(inside-cap? ray % 1.0))
         (map #(make-intersection % cylinder)))))

(defn- intersections-for-cylinder-wall
  [{:keys [transform minimum maximum] :as shape}
   {[px _ pz _] :point [dx _ dz _] :direction :as ray}]
   (let [a     (+ (* dx dx) (* dz dz))
         b     (* 2 (+ (* px dx) (* pz dz)))
         c     (+ (* px px) (* pz pz) -1)
         roots (quadratic-roots-for a b c)
         ts    (filter (fn [root]
                         (let [[_ y _ _ :as p] (r/position ray root)]
                           (< minimum y maximum))) roots)]
     (map #(make-intersection % shape) ts)))

(defmethod intersections-for :cylinder
  [{:keys [transform minimum maximum] :as shape} ray]
  (let [{:keys [point direction] :as local-ray} (r/transform ray (m/inverse transform))]
    (concat (intersections-for-cylinder-wall shape local-ray)
            (intersections-for-cylinder-caps shape local-ray))))

(defn- intersections-for-cone-caps
  [{:keys [capped? minimum maximum] :as cone}
   {[_ py _ _] :point [_ dy _ _] :direction :as ray}]
  (if (or (not capped?) (≈ 0.0 dy))
    []
    (let [candidate-ts    (map #(/ (- % py) dy) [minimum maximum])
          ;; Compute t values based on min and max y values
          radii           (map #(Math/abs %) [minimum maximum])
          args            (map #(vector ray %1 %2) candidate-ts radii)
          ;; Tuple up the ray with each radius and position
          filtered-tuples (filter #(apply inside-cap? %) args)
          ;; Filter out the ones not inside the caps for each y value
          ts              (map second filtered-tuples)]
      (map #(make-intersection % cone) ts))))

(defn- intersections-for-cone-wall
  [{:keys [transform minimum maximum] :as shape}
   {[px py pz _] :point [dx dy dz _] :direction :as ray}]
  ;; TODO: Possible bug here; we don't ever transform the ray below
   (let [a     (+ (* dx dx) (* -1.0 dy dy) (* dz dz))
         b     (* 2 (+ (* px dx) (* -1.0 py dy) (* pz dz)))
         c     (+ (* px px) (* -1.0 py py) (* pz pz))
         ts    (cond
                 (and (zero? a) (zero? b))
                   []
                 (zero? a)
                   [(/ c (* -2.0 b))]
                 :else
                   (filter (fn [root]
                             (let [[_ y _ _ :as p] (r/position ray root)]
                               (< minimum y maximum))) (quadratic-roots-for a b c)))]
     (map #(make-intersection % shape) ts)))

(defmethod intersections-for :cone
  [{:keys [transform minimum maximum] :as shape} ray]
  (let [{:keys [point direction] :as local-ray} (r/transform ray (m/inverse transform))]
    (concat (intersections-for-cone-wall shape local-ray)
            (intersections-for-cone-caps shape local-ray))))

(defmethod intersections-for :triangle
  [{:keys [transform p1 e1 e2] :as shape} ray]
  (let [{:keys [point direction] :as local-ray} (r/transform ray (m/inverse transform))
        ray-direction-cross-e2 (u/cross-product direction e2)
        determinant            (u/dot-product e1 ray-direction-cross-e2)
        f                      (/ 1.0 determinant)]
    (if (> ε (Math/abs determinant))
      ;; Is the ray parallel to the triangle?
      []
      (let [p1-to-ray-point    (u/subtract point p1)
            u                  (* f (u/dot-product p1-to-ray-point ray-direction-cross-e2))]
        (if (or (< u 0) (> u 1))
          ;; Does the ray not intersect the p1-p3 edge?
          []
          (let [ray-origin-cross-e1 (u/cross-product p1-to-ray-point e1)
                v                   (* f (u/dot-product direction ray-origin-cross-e1))]
            (if (or (< v 0) (> (+ u v) 1))
              ;; Does the ray fall anywhere else outside the triangle?
              []
              (let [t (* f (u/dot-product e2 ray-origin-cross-e1))]
                ;; We have a hit!!!
                [(make-intersection t shape)]))))))))

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
  [{:keys [transform] :as shape} ray]
  (let [{:keys [point direction] :as local-ray} (r/transform ray (m/inverse transform))
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

(defmethod local-normal-for :cylinder
  [{:keys [minimum maximum] :as shape}
   [x y z _ :as local-point]]
  (let [distance-squared (+ (* x x) (* z z))]
    (cond
      (and (< distance-squared 1) (>= y (- maximum ε)))
        [0 1 0 0]
      (and (< distance-squared 1) (<= y (+ minimum ε)))
        [0 -1 0 0]
      :else
        [x 0 z 0])))

(defmethod local-normal-for :cone
  [{:keys [minimum maximum] :as shape}
   [x y z _ :as local-point]]
  (let [distance-squared (+ (* x x) (* z z))]
    (cond
      (and (< distance-squared 1) (>= y (- maximum ε)))
        [0 1 0 0]
      (and (< distance-squared 1) (<= y (+ minimum ε)))
        [0 -1 0 0]
      (> y 0)
        [x (- (Math/sqrt distance-squared)) z 0]
      :else
        [x (Math/sqrt distance-squared) z 0])))

(defmethod local-normal-for :triangle
  [{:keys [normal]} _]
  normal)

(defn normal-for
  "This is the 'public' interface for computing the normal
   vector for any arbitrary type of shape. It first converts
   the world point to a local point, computes the normal in
   that coordinate system by deferring the specialized
   implementation for the shape, then transforms it back to the
   world coordinate system."
  [{:keys [transform] :as shape} world-point]
  (let [inverse-transform       (m/inverse transform)
        local-normal            (->> world-point
                                     (m/tuple-times inverse-transform)
                                     (local-normal-for shape))]
    (as-> local-normal $
          (m/tuple-times (m/transpose inverse-transform) $)
          (assoc $ 3 0)  ;; TODO: This is a hack per the book; look for better way
          (u/normalize $))))

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

(defmulti local-corners-for :shape-type)

(defmethod local-corners-for :sphere
  [_]
  (for [x [-1 1] y [-1 1] z [-1 1]]
    (vector x y z 1)))

(defmethod local-corners-for :cube
  [_]
  (for [x [-1 1] y [-1 1] z [-1 1]]
    (vector x y z 1)))

(defmethod local-corners-for :cone
  [{:keys [minimum maximum]}]
  (for [x [-1 1] y [minimum maximum] z [-1 1]]
    (vector x y z 1)))

(defmethod local-corners-for :cylinder
  [{:keys [minimum maximum]}]
  (for [x [-1 1] y [minimum maximum] z [-1 1]]
    (vector x y z 1)))

(defn eight-corners-for
  [{:keys [transform] :as shape}]
  (let [raw-corners (local-corners-for shape)]
    (map #(m/tuple-times transform %) raw-corners)))
