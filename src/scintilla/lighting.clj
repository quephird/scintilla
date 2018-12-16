(ns scintilla.lighting
  (:require [scintilla.color :as c]
            [scintilla.ray :as r]
            [scintilla.shapes :as s]
            [scintilla.tuple :as u]
            [clojure.pprint :refer :all]))

(defn make-light
  [position intensity]
  {:position position
   :intensity intensity})

(def default-light
  (make-light [-10 10 -10 1] [1 1 1]))

(defn shadowed?
  "Takes the light in the scene and a point and determines
   whether or not any object shadows that point."
  [{:keys [light] :as scene} point]
  (let [light-direction (-> light
                            :position
                            (u/subtract point))
        light-ray       (->> light-direction
                             u/normalize
                             (r/make-ray point))
        light-hit       (->> light-ray
                             (r/find-all-intersections scene)
                             (r/find-hit))]
    (and (not (nil? light-hit))
         (< (:t light-hit) (u/magnitude light-direction)))))

(defn ambient
  [{:keys [intensity] :as light}
   {:keys [shape] :as prepared-hit}]
  (let [{:keys [ambient]} (:material shape)
        color             (s/color-for prepared-hit)
        effective-color   (c/hadamard-product color intensity)]
    (c/scalar-times effective-color ambient)))

(defn diffuse
  [{:keys [intensity position] :as light}
   {:keys [shape surface-normal surface-point] :as prepared-hit}]
  (let [{diffuse :diffuse} (:material shape)
        color              (s/color-for prepared-hit)
        effective-color    (c/hadamard-product color intensity)
        light-vector       (u/normalize (u/subtract position surface-point))
        light-dot-normal   (u/dot-product light-vector surface-normal)]
    (if (< light-dot-normal 0)
        [0 0 0]
        (c/scalar-times effective-color (* diffuse light-dot-normal)))))

(defn specular
  [{:keys [intensity position] :as light}
   {:keys [eye-direction shape surface-normal surface-point] :as prepared-hit}]
  (let [{:keys [shininess specular]} (:material shape)
        light-vector                 (u/normalize (u/subtract position surface-point))
        light-dot-normal             (u/dot-product light-vector surface-normal)
        reflected-vector             (r/reflected-vector-for (u/subtract light-vector) surface-normal)
        reflect-dot-eye              (u/dot-product reflected-vector eye-direction)
        reflection-coefficient       (Math/pow reflect-dot-eye shininess)]
    (if (or (< light-dot-normal 0) (< reflection-coefficient 0))
        [0 0 0]
        (c/scalar-times intensity (* specular reflection-coefficient)))))

(defn lighting
  "Determines the color for the point associated with the hit
   and scene passed in; either it is the ambient color of the
   object associated with the hit, or the sum of all three
   possible contributions to the color."
  [{:keys [light] :as scene}
   {:keys [over-point] :as prepared-hit}]
  (if (shadowed? scene over-point)
    (ambient light prepared-hit)
    (c/add (ambient light prepared-hit)
           (diffuse light prepared-hit)
           (specular light prepared-hit))))

;; (defn- total-internal-reflection?
;;   [{:keys [n1 n2 eye-direction surface-normal] :as prepared-hit}]
;;   (let [cosθ₁  (u/dot-product eye-direction surface-normal)
;;         sin²θ₂ (* (Math/pow (/ n1 n2) 2) (- 1.0 (Math/pow cosθ₁ 2)))]
;;     (> sin²θ₂ 1.0)))

(def max-reflections 5)

(declare refracted-lighting)
(declare reflected-lighting)
(declare color-for)

(defn refracted-lighting
  ;;
  ;;                      |     
  ;;           n₂         | θ₂ ⟋
  ;;                      |  ⟋
  ;;         _____________|⟋_____________
  ;;                     /|
  ;;                    / |
  ;;           n₁      /θ₁|
  ;;                      |
  ;;
  [scene {:keys [t] :as prepared-hit} remaining-reflections]
  (let [transparency (get-in prepared-hit [:shape :material :transparency])]
    (if (or (zero? transparency)
            (zero? remaining-reflections))
      [0 0 0]
      (let [{:keys [n1 n2 eye-direction surface-normal under-point]} prepared-hit
            index-ratio  (/ n1 n2)
            cosθ₁        (u/dot-product eye-direction surface-normal)
            sin²θ₂       (* (Math/pow index-ratio 2) (- 1.0 (Math/pow cosθ₁ 2)))]
        (if (> sin²θ₂ 1.0)
          [0 0 0]
          (let [cosθ₂               (Math/sqrt (- 1.0 sin²θ₂))
                refracted-direction (u/subtract (u/scalar-times surface-normal (- (* index-ratio cosθ₁) cosθ₂))
                                                (u/scalar-times eye-direction index-ratio))
                refracted-ray       (r/make-ray under-point refracted-direction)
                refracted-color     (color-for scene refracted-ray (dec remaining-reflections))]
            (c/scalar-times refracted-color transparency)))))))

(defn reflected-lighting
  [scene prepared-hit remaining-reflections]
  (let [reflective (get-in prepared-hit [:shape :material :reflective])]
    (if (or (zero? reflective) (zero? remaining-reflections))
      [0 0 0]
      (let [{:keys [surface-point reflected-vector]} prepared-hit
            reflected-ray   (r/make-ray surface-point reflected-vector)
            reflected-color (color-for scene reflected-ray (dec remaining-reflections))]
        (c/scalar-times reflected-color reflective)))))

(defn color-for
  "For the given world and ray from the camera to the canvas,
   return the color correspondent to the hit object at the point
   of intersection or simply black if no object is hit."
  [{:keys [light] :as scene} ray remaining-reflections]
  (let [intersections  (r/find-all-intersections scene ray)
        hit            (r/find-hit intersections)]
    (if (nil? hit)
      [0 0 0]
      (let [prepared-hit       (r/make-prepared-hit hit ray intersections)
            primary-color      (lighting scene prepared-hit)
            reflected-color    (reflected-lighting scene prepared-hit remaining-reflections)]
        (c/add primary-color reflected-color)))))
