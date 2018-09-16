(ns scintilla.color-test
  (:require [clojure.test :refer :all]
            [scintilla.color :refer :all]
            [scintilla.numeric :refer [≈]]))

(deftest testing-plus
  (testing "adding two colors"
    (is (≈ [1.6 0.7 1.0] (+ [0.9 0.6 0.75] [0.7 0.1 0.25])))))

(deftest testing-minus
  (testing "adding two colors"
    (is (≈ [0.2 0.5 0.5] (- [0.9 0.6 0.75] [0.7 0.1 0.25])))))

(deftest testing-scalar-times
  (testing "adding two colors"
    (is (≈ [0.4 0.6 0.8] (* [0.2 0.3 0.4] 2)))))

(deftest testing-hadamard-product
  (testing "adding two colors"
    (is (≈ [0.9 0.2 0.04] (◦ [1 0.2 0.4] [0.9 1 0.1])))))
