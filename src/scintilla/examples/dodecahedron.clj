(ns scintilla.examples.dodecahedron
  (:require [scintilla.camera :as c]
            [scintilla.lighting :as l]
            [scintilla.numeric :refer :all]
            [scintilla.obj :as o]
            [scintilla.rendering :as r]
            [scintilla.scene :as e]
            [scintilla.transformation :as t]))

(defn render-obj-file
  []
  (let [main-group     (o/load-obj-file "scintilla/examples/dodecahedron.obj")
        light          (l/make-light [-5 10 -1 1] [1 1 1])
        scene          (e/make-scene [main-group] light)
        view-transform (t/view-transform-matrix-for [-1.5 1 -1.5 1]
                                                    [0 0 0 1]
                                                    [0 1 0 0])
        camera         (c/make-camera 400 400 π⟋2 view-transform)]
    (r/render-to-file camera scene "dodecahedron.ppm")))
