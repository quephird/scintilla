(ns scintilla.shapes-test
  (:require [clojure.test :refer :all]
            [scintilla.numeric :refer :all]
            [scintilla.shapes :refer :all]
            [scintilla.transformation :as t]))

(deftest testing-normal-for-sphere
  (testing "the normal on a sphere at a point on the x axis"
    (let [sphere (make-sphere)
          point  [1 0 0 1]
          expected-value [1 0 0 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a sphere at a point on the y axis"
    (let [sphere (make-sphere)
          point  [0 1 0 1]
          expected-value [0 1 0 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a sphere at a point on the z axis"
    (let [sphere (make-sphere)
          point  [0 0 1 1]
          expected-value [0 0 1 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a sphere at a non-axial point"
    (let [sphere (make-sphere)
          √3⟋3   (/ (Math/sqrt 3) 3.0)
          point  [√3⟋3 √3⟋3 √3⟋3 1]
          expected-value [√3⟋3 √3⟋3 √3⟋3 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a translated sphere"
    (let [transform (t/translation-matrix 0 1 0)
          sphere (make-sphere [1 0 0] transform)
          point  [0 1.70711 -0.70711 1]
          expected-value [0 0.70711 -0.70711 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a scaled sphere"
    (let [transform (t/scaling-matrix 1.0 0.5 1.0)
          sphere (make-sphere [1 0 0] transform)
          point  [0 0.70711 -0.70711 1]
          expected-value [0 0.97014 -0.24254 0]]
      (is (≈ expected-value (normal-for sphere point))))))

(deftest testing-normal-for-plane
  (testing "the normal on any point of a plane"
    (let [plane          (make-plane)
          points         [[0 0 0 1] [10 0 -10 1] [-5 0 150 1]]
          expected-value [0 1 0 0]]
      (is (every? #(≈ expected-value %) (map #(normal-for plane %) points))))))

(deftest testing-reflected-vector-for
  (testing "reflecting a vector approaching at 45°"
    (let [v [1 -1 0 0]
          n [0 1 0 0]
          expected-value [1 1 0 0]]
      (is (≈ expected-value (reflected-vector-for v n)))))
  (testing "reflecting a vector off a slanted surface"
    (let [v [0 -1 0 0]
          n [0.70711 0.70711 0 0]
          expected-value [1 0 0 0]]
      (is (≈ expected-value (reflected-vector-for v n))))))
