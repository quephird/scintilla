(ns scintilla.matrix-test
  (:require [scintilla.matrix :refer :all]
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
     (is (= I (transpose I)))))

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

  (testing "a 3X3 matrix"
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
      (is (= -25 (cofactor 1 0 a))))))

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
