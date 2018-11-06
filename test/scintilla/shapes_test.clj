(ns scintilla.shapes-test
  (:require [clojure.test :refer :all]
            [scintilla.numeric :refer :all]
            [scintilla.shapes :refer :all]))

(deftest testing-find-normal
  (testing "the normal on a sphere at a point on the x axis"
    (let [sphere (make-sphere [1 0 0])
          point  [1 0 0 1]
          expected-value [1 0 0 0]]
      (is (≈ expected-value (find-normal sphere point)))))
  (testing "the normal on a sphere at a point on the y axis"
    (let [sphere (make-sphere [1 0 0])
          point  [0 1 0 1]
          expected-value [0 1 0 0]]
      (is (≈ expected-value (find-normal sphere point)))))
  (testing "the normal on a sphere at a point on the z axis"
    (let [sphere (make-sphere [1 0 0])
          point  [0 0 1 1]
          expected-value [0 0 1 0]]
      (is (≈ expected-value (find-normal sphere point)))))
  (testing "the normal on a sphere at a non-axial point"
    (let [sphere (make-sphere [1 0 0])
          √3⟋3   (/ (Math/sqrt 3) 3.0)
          point  [√3⟋3 √3⟋3 √3⟋3 1]
          expected-value [√3⟋3 √3⟋3 √3⟋3 0]]
      (is (≈ expected-value (find-normal sphere point)))))
)
