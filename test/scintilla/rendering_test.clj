(ns scintilla.rendering-test
  (:require [clojure.test :refer :all]
            [scintilla.camera :as c]
            [scintilla.canvas :as v]
            [scintilla.materials :as a]
            [scintilla.matrix :refer [I₄]]
            [scintilla.numeric :refer :all]
            [scintilla.rendering :refer :all]
            [scintilla.scene :as e]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

(deftest testing-render
  (testing "rendering a world with a camera"
    (let [material1      (a/make-material [0.8 1.0 0.6] 0.1 0.7 0.2 200)
          sphere1        (s/make-sphere material1)
          transform2     (t/scaling-matrix 0.5 0.5 0.5)
          sphere2        (s/make-sphere a/default-material transform2)
          scene          (e/add-objects (e/make-scene) [sphere1 sphere2])
          from           [0 0 -5 1]
          to             [0 0 0 1]
          up             [0 1 0 0]
          view-transform (t/view-transform-matrix-for from to up)
          camera         (c/make-camera 11 11 π⟋2 view-transform)
          canvas         (render camera scene)
          expected-value [0.38066 0.47583 0.2855]]
      (is (≈ expected-value (v/read-pixel canvas 5 5))))))
