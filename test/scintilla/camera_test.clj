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
