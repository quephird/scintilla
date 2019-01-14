(ns scintilla.scene
  (:require [scintilla.matrix :as m]
            [scintilla.numeric :refer [ε]]
            [scintilla.ray :as r]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]
            [scintilla.tuple :as u]))

(defn make-scene
  [objects light]
  {:objects     objects
   :light       light})

(defn add-objects
  [scene objects]
  (update-in scene [:objects] concat objects))

(defmulti intersections-for
  (fn [{:keys [object-type] :as object} _]
    object-type))

(defmethod intersections-for :group
  [{:keys [children] :as group} ray]
  (->> children
       (map #(intersections-for % ray))
       (apply concat)))

(defmethod intersections-for :shape
  [shape ray]
  (s/intersections-for shape ray))

(defn all-intersections-for
  "Returns the set of all intersections that the given ray
   makes with the set of objects in the given scene."
  [{:keys [objects] :as scene} ray]
  (->> objects
       (map #(intersections-for % ray))
       (apply concat)
       (sort-by :t)))

(defn find-hit
  "Takes a set of intersections and selects only the
   'visible' one, which is the intesection with the least positive
   t value out of the set."
   [intersections]
   (->> intersections
        (sort-by :t)
        (some (fn [i] (if (< 0 (:t i)) i)))))

;; TODO: Need docstring and explanation of strategy
;;       Also need to rename to refractive-indices-for
(defn- derive-refractive-indices
  [hit all-intersections]
  (loop [n1          1.0
         n2          1.0
         encounters  []
         [x & xs]    all-intersections]
    (let [n1         (if (= (:t hit) (:t x))
                       (if (empty? encounters)
                         1.0
                         (get-in (last encounters) [:material :refractive-index])))
          encounters (if (some #{(:shape x)} encounters)
                       (remove #{(:shape x)} encounters)
                       (conj encounters (:shape x)))
          n2         (if (empty? encounters)
                       1.0
                       (get-in (last encounters) [:material :refractive-index]))]
      (if (= (:t hit) (:t x))
        {:n1 n1 :n2 n2}
        (recur n1 n2 encounters xs)))))

(defn make-prepared-hit
  "Returns a map representing the object hit by the ray
   with other pre-computed entities associated with it."
  [hit ray all-intersections]
  (let [surface-point    (r/position ray (:t hit))
        surface-normal   (s/normal-for (:shape hit) surface-point)
        eye-direction    (u/subtract (:direction ray))
        reflected-vector (r/reflected-vector-for (:direction ray) surface-normal)
        inside?          (> 0 (u/dot-product surface-normal eye-direction))
        ;; NOTA BENE: We "reset" the normal vector below if we are inside
        ;;            the hit object; in order for proper simulation of light
        ;;            we need to insure that the normal vector is always
        ;;            pointing towards the camera. Note also that both
        ;;            over-point and under-point need to be computed using
        ;;            _this_ normal vector, _not_ the one originally derived.
        surface-normal   (if inside?
                           (u/subtract surface-normal)
                           surface-normal)
        over-point       (u/plus surface-point (u/scalar-times surface-normal ε))
        under-point      (u/subtract surface-point (u/scalar-times surface-normal ε))
        {:keys [n1 n2]}  (derive-refractive-indices hit all-intersections)]
    (assoc hit
           :surface-point    surface-point
           :over-point       over-point
           :under-point      under-point
           :surface-normal   surface-normal
           :eye-direction    eye-direction
           :reflected-vector reflected-vector
           :inside           inside?
           :n1               n1
           :n2               n2)))
