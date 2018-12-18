(ns scintilla.camera-test
  (:require [clojure.test :refer :all]
            [scintilla.camera :refer :all]
            [scintilla.matrix :as m]
            [scintilla.numeric :refer :all]
            [scintilla.ray :as r]
            [scintilla.transformation :as t]))

(deftest testing-pixel-size-for
  (testing "the pixel size for a landscape canvas"
    (let [camera (make-camera 200 125 π⟋2)]
      (is (≈ 0.01 (pixel-size-for camera)))))
  (testing "the pixel size for a portrait canvas"
    (let [camera (make-camera 125 200 π⟋2)]
      (is (≈ 0.01 (pixel-size-for camera))))))

(deftest testing-ray-for
  (testing "constructing a ray through the center of the canvas"
    (let [camera (make-camera 201 101 π⟋2)
          ray    (ray-for camera 100 50)]
      (is (≈ [0 0 0 1] (:point ray)))
      (is (≈ [0 0 -1 0] (:direction ray)))))
  (testing "constructing a ray through the corner of the canvas"
    (let [camera (make-camera 201 101 π⟋2)
          ray    (ray-for camera 0 0)]
      (is (≈ [0 0 0 1] (:point ray)))
      (is (≈ [0.66519 0.33259 -0.66851] (:direction ray)))))
  (testing "constructing a ray through the center of the canvas"
    (let [R         (t/rotation-y-matrix π⟋4)
          T         (t/translation-matrix 0 -2 5)
          transform (m/matrix-times R T)
          camera    (make-camera 201 101 π⟋2 transform)
          ray       (ray-for camera 100 50)]
      (is (≈ [0 2 -5 1] (:point ray)))
      (is (≈ [0.70711 0 -0.70711 0] (:direction ray))))))
