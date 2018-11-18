(ns scintilla.transformation-test
  (:require [scintilla.transformation :refer :all]
            [scintilla.matrix :as m]
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
          T⁻¹ (m/inverse T)
          expected-value [-8 7 3 1]]
      (is (≈ expected-value (m/tuple-times T⁻¹ p))))))

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
          S⁻¹ (m/inverse S)
          expected-value [-2 2 2 0]]
      (is (≈ expected-value (m/tuple-times S⁻¹ v))))))

(deftest testing-rotation-x
  (let [p [0 1 0 1]]
    (testing "rotating a point around the x axis"
      (let [Rx (rotation-x-matrix π⟋4)
            expected-value [0.0 0.70711 0.70711 1.0]]
        (is (≈ expected-value (m/tuple-times Rx p))))
      (let [Rx (rotation-x-matrix π⟋2)
            expected-value [0.0 0.0 1.0 1.0]]
        (is (≈ expected-value (m/tuple-times Rx p)))))
    (testing "rotating a point using a rotation matrix for an angle
              is the same as using the inverse of a rotation matrix for the negative angle"
      (let [Rx (rotation-x-matrix π⟋4)
            Rx' (m/inverse (rotation-x-matrix (- π⟋4)))]
        (is (≈ (m/tuple-times Rx p) (m/tuple-times Rx' p)))))))

(deftest testing-rotation-y
  (let [p [0 0 1 1]]
    (testing "rotating a point around the y axis"
      (let [Ry (rotation-y-matrix π⟋4)
            expected-value [0.70711 0.0 0.70711 1.0]]
        (is (≈ expected-value (m/tuple-times Ry p))))
      (let [Ry (rotation-y-matrix π⟋2)
            expected-value [1.0 0.0 0.0 1.0]]
        (is (≈ expected-value (m/tuple-times Ry p)))))))

(deftest testing-rotation-z
  (let [p [0 1 0 1]]
    (testing "rotating a point around the z axis"
      (let [Rz (rotation-z-matrix π⟋4)
            expected-value [-0.70711 0.70711 0.0 1.0]]
        (is (≈ expected-value (m/tuple-times Rz p))))
      (let [Rz (rotation-z-matrix π⟋2)
            expected-value [-1.0 0.0 0.0 1.0]]
        (is (≈ expected-value (m/tuple-times Rz p)))))))

(deftest testing-shearing
  (testing "shearing x in proportion to y"
    (let [S (shearing-matrix 1 0 0 0 0 0)
          p [2 3 4 1]
          expected-value [5 3 4 1]]
      (is (≈ expected-value (m/tuple-times S p)))))
  (testing "shearing x in proportion to z"
    (let [S (shearing-matrix 0 1 0 0 0 0)
          p [2 3 4 1]
          expected-value [6 3 4 1]]
      (is (≈ expected-value (m/tuple-times S p)))))
  (testing "shearing y in proportion to x"
    (let [S (shearing-matrix 0 0 1 0 0 0)
          p [2 3 4 1]
          expected-value [2 5 4 1]]
      (is (≈ expected-value (m/tuple-times S p)))))
  (testing "shearing y in proportion to z"
    (let [S (shearing-matrix 0 0 0 1 0 0)
          p [2 3 4 1]
          expected-value [2 7 4 1]]
      (is (≈ expected-value (m/tuple-times S p)))))
  (testing "shearing z in proportion to x"
    (let [S (shearing-matrix 0 0 0 0 1 0)
          p [2 3 4 1]
          expected-value [2 3 6 1]]
      (is (≈ expected-value (m/tuple-times S p)))))
  (testing "shearing z in proportion to y"
    (let [S (shearing-matrix 0 0 0 0 0 1)
          p [2 3 4 1]
          expected-value [2 3 7 1]]
      (is (≈ expected-value (m/tuple-times S p))))))

(deftest testing-all-transformations
  (let [p  [1 0 1 1]
        Rx (rotation-x-matrix π⟋2)
        S  (scaling-matrix 5 5 5)
        T  (translation-matrix 10 5 7)]
    (testing "individual transformations applied in a sequence"
      (let [p₂ (m/tuple-times Rx p)
            p₃ (m/tuple-times S p₂)
            p₄ (m/tuple-times T p₃)]
        (is (≈ p₂ [1 -1 0 1]))
        (is (≈ p₃ [5 -5 0 1]))
        (is (≈ p₄ [15 0 7 1]))))
    (testing "chained transformations must be applied in reverse order"
      (let [M (->> Rx
                   (m/matrix-times S)
                   (m/matrix-times T))
            new-p (m/tuple-times M p)]
        (is (≈ new-p [15 0 7 1]))))))

(deftest testing-view-transform-matrix-for
  (testing "the transformation matrix for the default orientation"
    (let [from-point  [0 0 0 1]
          to-point    [0 0 -1 1]
          up-vector   [0 1 0 0]
          view-transform-matrix (view-transform-matrix-for from-point to-point up-vector)]
      (is (≈ view-transform-matrix m/I₄))))
  (testing "the transformation matrix for looking in positive z direction"
    (let [from-point  [0 0 0 1]
          to-point    [0 0 1 1]
          up-vector   [0 1 0 0]
          view-transform-matrix (view-transform-matrix-for from-point to-point up-vector)
          expected-value (scaling-matrix -1 1 -1)]
      (is (≈ view-transform-matrix expected-value))))
  (testing "an arbitrary view transformation"
    (let [from-point  [1 3 2 1]
          to-point    [4 -2 8 1]
          up-vector   [1 1 0 0]
          view-transform-matrix (view-transform-matrix-for from-point to-point up-vector)
          expected-value [[-0.50709  0.50709  0.67612 -2.36643]
                          [ 0.76772  0.60609  0.12122 -2.82843]
                          [-0.35857  0.59761 -0.71714  0.00000]
                          [ 0.00000  0.00000  0.00000  1.00000]]]
      (is (≈ view-transform-matrix expected-value)))))
