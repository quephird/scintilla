(ns scintilla.examples.teapot-low
  (:require [scintilla.camera :as c]
            [scintilla.lighting :as l]
            [scintilla.numeric :refer :all]
            [scintilla.obj :as o]
            [scintilla.rendering :as r]
            [scintilla.scene :as e]
            [scintilla.transformation :as t]))

(defn render-obj-file
  []
  (let [main-group     (o/load-obj-file "scintilla/examples/teapot_low.obj")
        light          (l/make-light [-15 10 10 1] [1 1 1])
        scene          (e/make-scene [main-group] light)
        view-transform (t/view-transform-matrix-for [0 25 10 1]
                                                    [0 0 7 1]
                                                    [0 0 1 0])
        camera         (c/make-camera 300 150 π⟋2 view-transform)]
    (r/render-to-file camera scene "teapot_low.ppm")))
