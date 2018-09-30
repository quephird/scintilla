(ns scintilla.matrix-test
  (:require [scintilla.matrix :refer :all]
            [clojure.test :refer :all]))

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
      (is (= expected-value (matrix-times a b))))))
