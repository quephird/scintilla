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
