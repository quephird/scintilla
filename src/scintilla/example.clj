(ns scintilla.example
  (:require [scintilla.camera :as c]
            [scintilla.lighting :as l]
            [scintilla.materials :as a]
            [scintilla.matrix :as m]
            [scintilla.numeric :refer :all]
            [scintilla.rendering :as r]
            [scintilla.scene :as e]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

(defn sphere-with-light
  []
  (let [transform      (t/scaling-matrix 0.5 0.5 0.5)
        material       (a/make-material [1 0.2 1] 0.1 0.9 0.9 20)
        sphere         (s/make-sphere material transform)
        light          (l/make-light [-10 10 -10 1] [1 1 1])
        scene          (e/make-scene [sphere] light)
        view-transform (t/view-transform-matrix-for [0 0 -1 1]
                                                    [0 0 0 1]
                                                    [0 1 0 0])
        camera         (c/make-camera 100 100 π⟋2 view-transform)]
    (r/render-to-file camera scene "sphere-with-light.ppm")))

(defn- make-floor
  []
  (let [material  (-> a/default-material
                      (a/set-color [1 0.9 0.9])
                      (a/set-specular 0.0))
        transform (t/scaling-matrix 10 0.01 10)]
    (s/make-sphere material transform)))

(defn- make-left-wall
  []
  (let [material  (-> a/default-material
                      (a/set-color [1 0.9 0.9])
                      (a/set-specular 0.0))
        transform (->> (t/scaling-matrix 10 0.01 10)
                       (m/matrix-times (t/rotation-x-matrix π⟋2))
                       (m/matrix-times (t/rotation-y-matrix (- π⟋4)))
                       (m/matrix-times (t/translation-matrix 0 0 5)))]
    (s/make-sphere material transform)))

(defn- make-right-wall
  []
  (let [material  (-> a/default-material
                      (a/set-color [1 0.9 0.9])
                      (a/set-specular 0.0))
        transform (->> (t/scaling-matrix 10 0.01 10)
                       (m/matrix-times (t/rotation-x-matrix π⟋2))
                       (m/matrix-times (t/rotation-y-matrix π⟋4))
                       (m/matrix-times (t/translation-matrix 0 0 5)))]
    (s/make-sphere material transform)))

(defn- make-left-sphere
  []
  (let [material  (-> a/default-material
                      (a/set-color [1 0.8 0.1])
                      (a/set-diffuse 0.7)
                      (a/set-specular 0.3))
        transform (-> (t/translation-matrix -1.5 0.33 -0.75)
                      (m/matrix-times (t/scaling-matrix 0.3 0.3 0.3)))]
    (s/make-sphere material transform)))

(defn- make-middle-sphere
  []
  (let [material  (-> a/default-material
                      (a/set-color [0.1 1.0 0.5])
                      (a/set-diffuse 0.7)
                      (a/set-specular 0.3))
        transform (t/translation-matrix -0.5 1 0.5)]
    (s/make-sphere material transform)))

(defn- make-right-sphere
  []
  (let [material  (-> a/default-material
                      (a/set-color [0.5 1 0.1])
                      (a/set-diffuse 0.7)
                      (a/set-specular 0.3))
        transform (-> (t/translation-matrix 1.5 0.5 -0.5)
                      (m/matrix-times (t/scaling-matrix 0.5 0.5 0.5)))]
    (s/make-sphere material transform)))

(defn three-spheres-in-corner
  []
  (let [floor          (make-floor)
        left-wall      (make-left-wall)
        right-wall     (make-right-wall)
        left-sphere    (make-left-sphere)
        middle-sphere  (make-middle-sphere)
        right-sphere   (make-right-sphere)
        scene          (e/make-scene [left-sphere middle-sphere right-sphere floor left-wall right-wall] l/default-light)
        view-transform (t/view-transform-matrix-for [0 1.5 -5 1]
                                                    [0 1 0 1]
                                                    [0 1 0 0])
        camera         (c/make-camera 100 50 π⟋3 view-transform)]
    (r/render-to-file camera scene "three-spheres-in-corner.ppm")))
