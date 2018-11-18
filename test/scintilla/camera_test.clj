(ns scintilla.camera-test
  (:require [clojure.test :refer :all]
            [scintilla.camera :refer :all]
            [scintilla.numeric :refer :all]))

(deftest testing-pixel-size-for
  (testing "the pixel size for a landscape canvas"
    (let [camera (make-camera 200 125 π⟋2)]
      (is (≈ 0.01 (pixel-size-for camera)))))
  (testing "the pixel size for a portrait canvas"
    (let [camera (make-camera 125 200 π⟋2)]
      (is (≈ 0.01 (pixel-size-for camera))))))
