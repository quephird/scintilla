(ns scintilla.rendering-test
  (:require [clojure.test :refer :all]
            [scintilla.camera :as c]
            [scintilla.canvas :as v]
            [scintilla.lighting :as l]
            [scintilla.materials :as a]
            [scintilla.numeric :refer :all]
            [scintilla.rendering :refer :all]
            [scintilla.scene :as e]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

(deftest testing-render
  (testing "rendering a world with a camera"
    (let [material1      (a/make-material {:color [0.8 1.0 0.6]
                                           :ambient 0.1
                                           :diffuse 0.7
                                           :specular 0.2
                                           :shininess 200
                                           :pattern nil})
          sphere1        (s/make-sphere {:material material1})
          transform2     (t/scaling-matrix 0.5 0.5 0.5)
          sphere2        (s/make-sphere {:material a/default-material
                                         :transform transform2})
          scene          (e/make-scene [sphere1 sphere2] l/default-light)
          from           [0 0 -5 1]
          to             [0 0 0 1]
          up             [0 1 0 0]
          view-transform (t/view-transform-matrix-for from to up)
          camera         (c/make-camera 11 11 π⟋2 view-transform)
          canvas         (render camera scene)
          expected-value [0.38066 0.47583 0.2855]]
      (is (≈ expected-value (v/read-pixel canvas 5 5))))))
