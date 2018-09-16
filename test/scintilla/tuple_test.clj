(ns scintilla.tuple-test
  (:require [clojure.test :refer :all]
            [scintilla.numeric :refer [≈]]
            [scintilla.tuple :refer :all]))

(deftest testing-tuples
  (testing "[1 2 3 1] is a point"
    (is (true? (point? [1 2 3 1]))))

  (testing "[1 2 3 0] is a vector"
    (is (true? (vector? [1 2 3 0])))))

(deftest making-tuples
  (testing "make-point"
    (is (point? (make-point 1 2 3))))

  (testing "make-vector"
    (is (vector? (make-vector 1 2 3)))))

(deftest testing-plus
  (testing "adding a point to a vector results in a new point"
    (let [result (+ [1 2 3 1] [2 3 4 0])]
      (is (= [3 5 7 1] result))
      (is (point? result))))

  (testing "adding a vector to a point results in a new point"
    (let [result (+ [1 2 3 0] [2 3 4 1])]
      (is (= [3 5 7 1] result))
      (is (point? result))))

  (testing "adding a vector to a vector results in a new vector"
    (let [result (+ [1 2 3 0] [2 3 4 0])]
      (is (= [3 5 7 0] result))
      (is (vector? result)))))

(deftest testing-minus
  (testing "subtracting one point from another results in a vector"
    (let [result (- [3 2 1 1] [5 6 7 1])]
      (is (= [-2 -4 -6 0] result))
      (is (vector? result))))

  (testing "subtracting a vector from a point results in a point"
    (let [result (- [3 2 1 1] [5 6 7 0])]
      (is (= [-2 -4 -6 1] result))
      (is (point? result))))

  (testing "subtracting one vector from another results in a vector"
    (let [result (- [3 2 1 0] [5 6 7 0])]
      (is (= [-2 -4 -6 0] result))
      (is (vector? result)))))

(deftest testing-times
  (testing "multiplying a tuple by a scalar"
    (is (= [-3.5 7.0 -10.5 0] (* [-1 2 -3 0] 3.5)))))

(deftest testing-divided-by
  (testing "dividing a tuple by a scalar"
    (is (= [-0.5 1.0 -1.5 0] (/ [-1 2 -3 0] 2)))))

(deftest testing-negate
  (testing "negating a tuple"
    (is (= [-1 -2 -3 0] (- [1 2 3 0])))))

(deftest testing-magnitude
  (testing "vector with only x component"
    (is (= 1.0 (magnitude [1 0 0 0]))))
  (testing "vector with only y component"
    (is (= 1.0 (magnitude [0 1 0 0]))))
  (testing "vector with only z component"
    (is (= 1.0 (magnitude [0 0 1 0]))))
  (testing "vector with all three components"
    (is (= (Math/sqrt 14.0) (magnitude [1 2 3 0]))))
  (testing "vector with all three components negative still has positive magnitude"
    (is (= (Math/sqrt 14.0) (magnitude [-1 -2 -3 0])))))

(deftest testing-normalize
  (testing "vector with only one component should normalize to a unit vector in that direction"
    (is (≈ [1 0 0 0] (normalize [4 0 0 0]))))
  (testing "vector with three nonzero components"
    (is (≈ [0.26726 0.53452 0.80178] (normalize [1 2 3 0]))))
  (testing "magnitude of normalized vector"
    (is (≈ 1 (magnitude (normalize [1 2 3 0]))))))

(deftest testing-dot-product
  (testing "two vectors' dot product should be a scalar value"
    (is (= 20 (⋅ [1 2 3 0] [2 3 4 0])))))

(deftest testing-cross-product
  (let [a [1 2 3 0]
        b [2 3 4 0]]
    (testing "two vectors cross product should be another vector"
      (is (= [-1 2 -1 0] (⨯ a b))))
    (testing "swapping the two operands results in the same vector but in the opposite direction"
      (is (= (⨯ a b) (- (⨯ b a)))))))
