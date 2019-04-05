(ns scintilla.examples.pyramid
  (:require [scintilla.camera :as c]
            [scintilla.lighting :as l]
            [scintilla.materials :as a]
            [scintilla.matrix :as m]
            [scintilla.numeric :refer :all]
            [scintilla.patterns :as p]
            [scintilla.rendering :as r]
            [scintilla.scene :as e]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

(defn pyramid
  []
  (let [floor-material (a/make-material {:color [0.5 0.5 0.5]})
        floor          (s/make-plane floor-material)

        vertices       [[[0 1.8 0 1] [-1 0 -1 1] [1 0 -1 1]]
                        [[0 1.8 0 1] [1 0 -1 1] [1 0 1 1]]
                        [[0 1.8 0 1] [1 0 1 1] [-1 0 1 1]]
                        [[0 1.8 0 1] [-1 0 1 1] [-1 0 -1 1]]]
        faces          (map-indexed
                        (fn [idx [p1 p2 p3]]
                          (s/make-triangle p1 p2 p3
                                            {:material
                                             (a/make-material {:color [1 (rem idx 2) 0]})}))
                        vertices)

        scene          (e/make-scene (cons floor faces) l/default-light)
        view-transform (t/view-transform-matrix-for [-1 1.5 -5 1]
                                                    [0 1 0 1]
                                                    [0 1 0 0])
        camera         (c/make-camera 400 400 π⟋3 view-transform)]
    (r/render-to-file camera scene "pyramid.ppm"))  )
