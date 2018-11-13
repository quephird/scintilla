(ns scintilla.example
  (:require [scintilla.lighting :as l]
            [scintilla.materials :as a]
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
