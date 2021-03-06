(ns scintilla.ray-test
  (:require [clojure.test :refer :all]
            [scintilla.camera :as c]
            [scintilla.matrix :as m]
            [scintilla.numeric :refer :all]
            [scintilla.ray :refer :all]
            [scintilla.transformation :as t]))

(deftest testing-position
  (testing "computing the position of the end of a ray for different values of t"
    (let [ray (make-ray [2 3 4 1] [1 0 0 0])]
      (is (≈ [2 3 4 1] (position ray 0)))
      (is (≈ [3 3 4 1] (position ray 1)))
      (is (≈ [1 3 4 1] (position ray -1)))
      (is (≈ [4.5 3 4 1] (position ray 2.5))))))

(deftest testing-transform
  (testing "translating a ray has no effect on its direction vector"
    (let [ray (make-ray [1 2 3 1] [0 1 0 0])
          {:keys [point direction]} (make-ray [4 6 8 1] [0 1 0 0])
          T (t/translation-matrix 3 4 5)]
      (is (≈ point (:point (transform ray T))))
      (is (≈ direction (:direction (transform ray T))))))
  (testing "scaling a ray transforms both its point and direction vector"
    (let [ray (make-ray [1 2 3 1] [0 1 0 0])
          {:keys [point direction]} (make-ray [2 6 12 1] [0 3 0 0])
          S (t/scaling-matrix 2 3 4)]
      (is (≈ point (:point (transform ray S))))
      (is (≈ direction (:direction (transform ray S)))))))

(deftest testing-reflected-vector-for
  (testing "reflecting a vector approaching at 45°"
    (let [v [1 -1 0 0]
          n [0 1 0 0]
          expected-value [1 1 0 0]]
      (is (≈ expected-value (reflected-vector-for v n)))))
  (testing "reflecting a vector off a slanted surface"
    (let [v [0 -1 0 0]
          n [0.70711 0.70711 0 0]
          expected-value [1 0 0 0]]
      (is (≈ expected-value (reflected-vector-for v n))))))
