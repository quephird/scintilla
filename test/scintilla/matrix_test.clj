(ns scintilla.matrix-test
  (:require [scintilla.matrix :refer :all]
            [scintilla.numeric :refer [≈]]
            [clojure.test :refer :all]))

(deftest testing-transpose
  (testing "transposing a 4x4 matrix"
    (let [a [[0 9 3 0]
             [9 8 0 8]
             [1 8 5 3]
             [0 0 5 8]]
          expected-value [[0 9 1 0]
                          [9 8 8 0]
                          [3 0 5 5]
                          [0 8 3 8]]]
     (is (= expected-value (transpose a)))))
  (testing "transposing the identity matrix"
     (is (= I₄ (transpose I₄)))))

(deftest testing-matrix-times
  (testing "multiplying two 4X4 matrices"
    (let [a [[1 2 3 4]
             [2 3 4 5]
             [3 4 5 6]
             [4 5 6 7]]
          b [[0 1 2 4]
             [1 2 4 8]
             [2 4 8 16]
             [4 8 16 32]]
          expected-value [[24 49 98 196]
                          [31 64 128 256]
                          [38 79 158 316]
                          [45 94 188 376]]]
      (is (= expected-value (matrix-times a b)))))

(deftest testing-tuple-times
  (testing "multiplying a 4X4 matrix with a 4 element tuple"
    (let [a [[1 2 3 4]
             [2 4 4 2]
             [8 6 4 1]
             [0 0 0 1]]
          b [1 2 3 1]
          expected-value [18 24 33 1]]
      (is (= expected-value (tuple-times a b))))))

(deftest testing-scalar-times
  (testing "multiplying a 4X4 matrix with a scalar"
    (let [a [[1 2 3 4]
             [2 4 4 2]
             [8 6 4 1]
             [0 0 0 1]]
          b 2
          expected-value [[2 4 6 8]
                          [4 8 8 4]
                          [16 12 8 2]
                          [0 0 0 2]]]
      (is (= expected-value (scalar-times a b)))))))

(deftest testing-submatrix
  (testing "a 3X3 matrix"
    (let [a [[1 5 0]
             [-3 2 7]
             [0 6 -3]]
          expected-value [[-3 2]
                          [0 6]]]
      (is (= expected-value (submatrix 0 2 a)))))

  (testing "a 4X4 matrix"
    (let [a [[-6 1 1 6]
             [-8 5 8 6]
             [-1 0 8 2]
             [-7 1 -1 1]]
          expected-value [[-6 1 6]
                          [-8 8 6]
                          [-7 -1 1]]]
      (is (= expected-value (submatrix 2 1 a))))))

(deftest testing-minor
  (testing "a 3X3 matrix"
    (let [a [[3 5 0]
             [2 -1 -7]
             [6 -1 5]]]
      (is (= 25 (minor 1 0 a))))))

(deftest testing-cofactor
  (testing "a 3X3 matrix"
    (let [a [[3 5 0]
             [2 -1 -7]
             [6 -1 5]]]
      (is (= -12 (cofactor 0 0 a)))
      (is (= -25 (cofactor 1 0 a)))))
  (testing "a 4X4 matrix"
    (let [a [[-2 -8 3 5]
             [-3 1 7 3]
             [1 2 -9 6]
             [-6 7 7 -9]]]
      (is (= 690 (cofactor 0 0 a)))
      (is (= 447 (cofactor 0 1 a)))
      (is (= 210 (cofactor 0 2 a)))
      (is (= 51 (cofactor 0 3 a))))))

(deftest testing-determinant
  (testing "a 2X2 matrix"
    (let [a [[1 5]
             [-3 2]]]
      (is (= 17 (determinant a)))))
  (testing "a 3X3 matrix"
    (let [a [[1 2 6]
             [-5 8 -4]
             [2 6 4]]]
      (is (= -196 (determinant a)))))
  (testing "a 4X4 matrix"
    (let [a [[-2 -8 3 5]
             [-3 1 7 3]
             [1 2 -9 6]
             [-6 7 7 -9]]]
      (is (= -4071 (determinant a))))))

(deftest testing-cofactor-matrix
  (testing "a 4X4 matrix"
    (let [a [[-5 2 6 -8]
             [1 -5 1 8]
             [7 7 -6 -7]
             [1 -3 7 4]]
          expected-value [[116 -430 -42 -278]
                          [240 -775 -119 -433]
                          [128 -236 -28 -160]
                          [-24 277 105 163]]]
      (is (= expected-value (cofactor-matrix a))))))

(deftest testing-inverse
  (testing "a 4X4 matrix"
    (let [a [[-5 2 6 -8]
             [1 -5 1 8]
             [7 7 -6 -7]
             [1 -3 7 4]]
          expected-value [[0.21805 0.45113 0.24060 -0.04511]
                          [-0.80827 -1.45677 -0.44361 0.52068]
                          [-0.07895 -0.22368 -0.05263 0.19737]
                          [-0.52256 -0.81391 -0.30075 0.30639]]]
      (is (≈ expected-value (inverse a)))))
  (testing "a 4X4 matrix"
    (let [a [[8 -5 9 2]
             [7 5 6 1]
             [-6 0 9 6]
             [-3 0 -9 -4]]
          expected-value [[-0.15385 -0.15385 -0.28205 -0.53846]
                          [-0.07692 0.12308 0.02564 0.03077]
                          [0.35897 0.35897 0.43590 0.92308]
                          [-0.69231 -0.69231 -0.76923 -1.92308]]]
      (is (≈ expected-value (inverse a)))))
  (testing "a 4X4 matrix"
    (let [a [[9 3 0 9]
             [-5 -2 -6 -3]
             [-4 9 6 4]
             [-7 6 6 2]]
          expected-value [[-0.04074 -0.07778 0.14444 -0.22222]
                          [-0.07778 0.03333 0.36667 -0.33333]
                          [-0.02901 -0.14630 -0.10926 0.12963]
                          [0.17778 0.06667 -0.26667 0.33333]]]
      (is (≈ expected-value (inverse a)))))
  (testing "the inverse of the identity matrix"
    (is (≈ I₄ (inverse I₄))))
  (testing "the product of two matrices and the inverse of the second one"
    (let [a [[3 -9 7 3]
             [3 -8 2 -9]
             [-4 4 4 1]
             [-6 5 -1 1]]
          b [[8 2 2 2]
             [3 -1 7 0]
             [7 0 5 4]
             [6 -2 0 5]]
          b⁻¹ (inverse b)]
     (is (≈ a (matrix-times (matrix-times a b) b⁻¹))))))
