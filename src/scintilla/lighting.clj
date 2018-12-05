(ns scintilla.lighting
  (:require [scintilla.color :as c]
            [scintilla.ray :as r]
            [scintilla.shapes :as s]
            [scintilla.tuple :as u]))

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
   {:keys [surface-point] :as prepared-hit}]
  (if (shadowed? scene surface-point)
    (ambient light prepared-hit)
    (c/add (ambient light prepared-hit)
           (diffuse light prepared-hit)
           (specular light prepared-hit))))

(defn color-for
  "For the given world and ray from the camera to the canvas,
   return the color correspondent to the hit object or simply
   black if no object is hit."
  [{:keys [light] :as scene} ray]
  (let [hit (-> scene
               (r/find-all-intersections ray)
               (r/find-hit))]
  (if (nil? hit)
      [0 0 0]
      (as-> hit $
            (r/make-prepared-hit $ ray)
            (lighting scene $)))))
