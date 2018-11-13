(ns scintilla.lighting
  (:require [scintilla.color :as c]
            [scintilla.shapes :as s]
            [scintilla.tuple :refer :all]))

(defn make-light
  [position intensity]
  {:position position
   :intensity intensity})

(def default-light
  (make-light [-10 10 -10 1] [1 1 1]))

(defn ambient
  [{:keys [intensity] :as light}
   {:keys [shape] :as prepared-hit}]
  (let [{:keys [color ambient]} (:material shape)
        effective-color         (c/hadamard-product color intensity)]
    (c/scalar-times effective-color ambient)))

(defn diffuse
  [{:keys [intensity position] :as light}
   {:keys [shape surface-normal surface-point] :as prepared-hit}]
  (let [{color :color diffuse :diffuse} (:material shape)
        effective-color         (c/hadamard-product color intensity)
        light-vector            (normalize (- position surface-point))
        light-dot-normal        (⋅ light-vector surface-normal)
        ]
    (if (< light-dot-normal 0)
        [0 0 0]
        (c/scalar-times effective-color (clojure.core/* diffuse light-dot-normal)))))

(defn specular
  [{:keys [intensity position] :as light}
   {:keys [eye-direction shape surface-normal surface-point] :as prepared-hit}]
  (let [{:keys [shininess specular]} (:material shape)
        light-vector                 (normalize (- position surface-point))
        light-dot-normal             (⋅ light-vector surface-normal)
        reflection-vector            (s/find-reflection (* light-vector -1.0) surface-normal)
        reflect-dot-eye              (⋅ reflection-vector eye-direction)
        reflection-coefficient       (Math/pow reflect-dot-eye shininess)]
    (if (or (< light-dot-normal 0) (< reflection-coefficient 0))
        [0 0 0]
        (c/scalar-times intensity (clojure.core/* specular reflection-coefficient)))))

(defn lighting
  [light prepared-hit]
  (c/add (ambient light prepared-hit)
         (diffuse light prepared-hit)
         (specular light prepared-hit)))
