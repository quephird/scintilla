(ns scintilla.ray-test
  (:require [clojure.test :refer :all]
            [scintilla.ray :refer :all]
            [scintilla.shapes :refer :all]
            [scintilla.numeric :refer [≈]]))

(deftest testing-position
  (testing "computing the position of the end of a ray for different values of t"
    (let [ray (make-ray [2 3 4 1] [1 0 0 0])]
      (is (≈ [2 3 4 1] (position ray 0)))
      (is (≈ [3 3 4 1] (position ray 1)))
      (is (≈ [1 3 4 1] (position ray -1)))
      (is (≈ [4.5 3 4 1] (position ray 2.5))))))

(deftest testing-intersections
  (testing "a ray that intersects a sphere at two points"
    (let [ray    (make-ray [0 0 -5 1] [0 0 1 0])
          sphere (make-sphere [0 0 0 1] 1)
          points (find-intersections sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [4.0 6.0] (mapv :t points)))))
  (testing "a ray that intersects a sphere at one point"
    (let [ray    (make-ray [0 1 -5 1] [0 0 1 0])
          sphere (make-sphere [0 0 0 1] 1)
          points (find-intersections sphere ray)]
      (is (= 1 (count points)))
      (is (≈ [5.0] (mapv :t points)))))
  (testing "a ray that intersects a sphere at one point"
    (let [ray    (make-ray [0 2 -5 1] [0 0 1 0])
          sphere (make-sphere [0 0 0 1] 1)
          points (find-intersections sphere ray)]
      (is (= 0 (count points)))))
  (testing "a ray that originates from within a sphere"
    (let [ray    (make-ray [0 0 0 1] [0 0 1 0])
          sphere (make-sphere [0 0 0 1] 1)
          points (find-intersections sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [-1.0 1.0] (mapv :t points)))))
  (testing "a ray that originates in front of a sphere"
    (let [ray    (make-ray [0 0 5 1] [0 0 1 0])
          sphere (make-sphere [0 0 0 1] 1)
          points (find-intersections sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [-6.0 -4.0] (mapv :t points))))))
