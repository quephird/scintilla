(ns scintilla.transformation-test
  (:require [scintilla.transformation :refer :all]
            [scintilla.matrix :refer :all]
            [scintilla.numeric :refer [≈]]
            [clojure.test :refer :all]))

(deftest testing-translation
  (testing "translating a point using a transformation matrix"
    (let [p [-3 4 5 1]
          expected-value [2 1 7 1]]
      (is (= expected-value (translate p 5 -3 2)))))
  (testing "translating a point with the inverse of a transformation matrix"
    (let [p [-3 4 5 1]
          T (translation-matrix 5 -3 2)
          T⁻¹ (inverse T)
          expected-value [-8 7 3 1]]
      (is (≈ expected-value (tuple-times T⁻¹ p))))))

(deftest testing-scaling
  (testing "scaling a point"
    (let [p [-4 6 8 1]
          expected-value [-8 18 32 1]]
      (is (≈ expected-value (scale p 2 3 4)))))
  (testing "scaling a vector"
    (let [v [-4 6 8 0]
          expected-value [-8 18 32 0]]
      (is (≈ expected-value (scale v 2 3 4)))))
  (testing "scaling a vector with the inverse of a scaling matrix"
    (let [v [-4 6 8 0]
          S (scaling-matrix 2 3 4)
          S⁻¹ (inverse S)
          expected-value [-2 2 2 0]]
      (is (≈ expected-value (tuple-times S⁻¹ v))))))

(deftest testing-rotation-x
  (let [p [0 1 0 1]]
    (testing "rotating a point around the x axis"
      (let [Rx (rotation-x-matrix π⟋4)
            expected-value [0.0 0.70711 0.70711 1.0]]
        (is (≈ expected-value (tuple-times Rx p))))
      (let [Rx (rotation-x-matrix π⟋2)
            expected-value [0.0 0.0 1.0 1.0]]
        (is (≈ expected-value (tuple-times Rx p)))))
    (testing "rotating a point using a rotation matrix for an angle
              is the same as using the inverse of a rotation matrix for the negative angle"
      (let [Rx (rotation-x-matrix π⟋4)
            Rx' (inverse (rotation-x-matrix (- π⟋4)))]
        (is (≈ (tuple-times Rx p) (tuple-times Rx' p)))))))

(deftest testing-rotation-y
  (let [p [0 0 1 1]]
    (testing "rotating a point around the y axis"
      (let [Ry (rotation-y-matrix π⟋4)
            expected-value [0.70711 0.0 0.70711 1.0]]
        (is (≈ expected-value (tuple-times Ry p))))
      (let [Ry (rotation-y-matrix π⟋2)
            expected-value [1.0 0.0 0.0 1.0]]
        (is (≈ expected-value (tuple-times Ry p)))))))

(deftest testing-rotation-z
  (let [p [0 1 0 1]]
    (testing "rotating a point around the z axis"
      (let [Rz (rotation-z-matrix π⟋4)
            expected-value [-0.70711 0.70711 0.0 1.0]]
        (is (≈ expected-value (tuple-times Rz p))))
      (let [Rz (rotation-z-matrix π⟋2)
            expected-value [-1.0 0.0 0.0 1.0]]
        (is (≈ expected-value (tuple-times Rz p)))))))

(deftest testing-shearing
  (testing "shearing x in proportion to y"
    (let [S (shearing-matrix 1 0 0 0 0 0)
          p [2 3 4 1]
          expected-value [5 3 4 1]]
      (is (≈ expected-value (tuple-times S p)))))
  (testing "shearing x in proportion to z"
    (let [S (shearing-matrix 0 1 0 0 0 0)
          p [2 3 4 1]
          expected-value [6 3 4 1]]
      (is (≈ expected-value (tuple-times S p)))))
  (testing "shearing y in proportion to x"
    (let [S (shearing-matrix 0 0 1 0 0 0)
          p [2 3 4 1]
          expected-value [2 5 4 1]]
      (is (≈ expected-value (tuple-times S p)))))
  (testing "shearing y in proportion to z"
    (let [S (shearing-matrix 0 0 0 1 0 0)
          p [2 3 4 1]
          expected-value [2 7 4 1]]
      (is (≈ expected-value (tuple-times S p)))))
  (testing "shearing z in proportion to x"
    (let [S (shearing-matrix 0 0 0 0 1 0)
          p [2 3 4 1]
          expected-value [2 3 6 1]]
      (is (≈ expected-value (tuple-times S p)))))
  (testing "shearing z in proportion to y"
    (let [S (shearing-matrix 0 0 0 0 0 1)
          p [2 3 4 1]
          expected-value [2 3 7 1]]
      (is (≈ expected-value (tuple-times S p))))))

(deftest testing-all-transformations
  (let [p  [1 0 1 1]
        Rx (rotation-x-matrix π⟋2)
        S  (scaling-matrix 5 5 5)
        T  (translation-matrix 10 5 7)]
    (testing "individual transformations applied in a sequence"
      (let [p₂ (tuple-times Rx p)
            p₃ (tuple-times S p₂)
            p₄ (tuple-times T p₃)]
        (is (≈ p₂ [1 -1 0 1]))
        (is (≈ p₃ [5 -5 0 1]))
        (is (≈ p₄ [15 0 7 1]))))
    (testing "chained transformations must be applied in reverse order"
      (let [M (->> Rx
                   (matrix-times S)
                   (matrix-times T))
            new-p (tuple-times M p)]
        (is (≈ new-p [15 0 7 1]))))))
