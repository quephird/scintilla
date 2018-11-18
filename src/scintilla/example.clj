(ns scintilla.example
  (:require [scintilla.lighting :as l]
            [scintilla.materials :as a]
            [scintilla.matrix :as m]
            [scintilla.rendering :as r]
            [scintilla.scene :as e]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

(defn sphere-with-light
  []
  (let [transform (t/scaling-matrix 0.5 0.5 0.5)
        material (a/make-material [1 0.2 1] 0.1 0.9 0.9 20)
        sphere  (s/make-sphere [0 1 0] transform material)
        light (l/make-light [-10 10 -10 1] [1 1 1])
        scene (e/make-scene [sphere] light)
        canvas-dimensions [100 100]]
    (r/render-to-file scene canvas-dimensions "sphere-with-light.ppm")))

(defn four-spheres-with-light
  []
  (let [S            (t/scaling-matrix 0.5 0.5 0.5)
        coordinates  [[-1 1 0] [1 1 0] [1 -1 0] [-1 -1 0]]
        translations (map #(apply t/translation-matrix %) coordinates)
        transforms   (map #(m/matrix-times S %) translations)
        colors       [[0.3 0 0.5] [0 0.3 0.7] [0.9 0.3 0.2] [1 0 0.5]]
        materials    (map #(a/make-material % 0.1 0.9 0.9 20) colors)
        spheres      (map #(s/make-sphere [0 1 0] %1 %2) transforms materials)
        light        (l/make-light [-10 10 -10 1] [1 1 1])
        scene        (e/make-scene spheres light)
        canvas-dimensions [400 400]]
    (r/render-to-file scene canvas-dimensions "four-spheres-with-light.ppm")))
