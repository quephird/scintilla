(ns scintilla.tuple-test
  (:require [clojure.test :refer :all]
            [scintilla.tuple :as subject]))

(deftest testing-tuples
  (testing "[1 2 3 1] is a point"
    (is (true? (subject/point? [1 2 3 1]))))

  (testing "[1 2 3 0] is a vector"
    (is (true? (subject/vector? [1 2 3 0])))))

(deftest making-tuples
  (testing "make-point"
    (is (subject/point? (subject/make-point 1 2 3))))

  (testing "make-veector"
    (is (subject/vector? (subject/make-vector 1 2 3)))))
