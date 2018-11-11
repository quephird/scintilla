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
  [material light surface-position]
  (let [effective-color (c/hadamard-product (:color material) (:intensity light))]
    (c/scalar-times effective-color (:ambient material))))

(defn diffuse
  [material light surface-position surface-normal]
  (let [effective-color  (c/hadamard-product (:color material) (:intensity light))
        light-vector     (normalize (- (:position light) surface-position))
        light-dot-normal (⋅ light-vector surface-normal)]
    (if (< light-dot-normal 0)
        [0 0 0]
        (c/scalar-times effective-color (clojure.core/* (:diffuse material) light-dot-normal)))))

(defn specular
  [material light surface-position eye-direction surface-normal]
  (let [light-vector      (normalize (- (:position light) surface-position))
        light-dot-normal  (⋅ light-vector surface-normal)
        reflection-vector (s/find-reflection (* light-vector -1.0) surface-normal)
        reflect-dot-eye   (⋅ reflection-vector eye-direction)
        reflection-coefficient (Math/pow reflect-dot-eye (:shininess material))]
    (if (or (< light-dot-normal 0) (< reflection-coefficient 0))
        [0 0 0]
        (c/scalar-times (:intensity light) (clojure.core/* (:specular material) reflection-coefficient)))))

(defn lighting
  [material light surface-position eye-direction surface-normal]
  (c/add (ambient material light surface-position)
         (diffuse material light surface-position surface-normal)
         (specular material light surface-position eye-direction surface-normal)))
