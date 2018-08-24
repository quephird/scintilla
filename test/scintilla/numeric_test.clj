(ns scintilla.numeric-test
  (:require [clojure.test :refer :all]
            [scintilla.numeric :refer :all]))

(deftest testing-doubles
  (testing "two Doubles are not close enough"
    (is (false? (≈ 2.0 2.1))))

  (testing "two Doubles are close enough"
    (is (true? (≈ 2.0 2.000001)))))

(deftest testing-vectors
  (testing "two vectors with one pair of numbers that are not close enough"
    (is (false? (≈ [1 2 3]
                   [1.1 2.0 3.00000001]))))

  (testing "two vectors with all pairs that are close enough"
    (is (true? (≈ [1 2 3]
                  [1.000001 1.999991 3.000001])))))

(deftest testing-plus
  (testing "adding a point to a vector results in a new point"
    (is (= [3 5 7 1] (plus [1 2 3 1] [2 3 4 0]))))

  (testing "adding a vector to a vector results in a new vector"
    (is (= [3 5 7 0] (plus [1 2 3 0] [2 3 4 0])))))
