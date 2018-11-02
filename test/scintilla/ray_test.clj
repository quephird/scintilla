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

(deftest testing-find-intersections
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

(deftest testing-find-hit
  (let [s (make-sphere [0 0 0 1] 1)]
    (testing "when all intersections have positive t"
      (let [i1 (make-intersection 1 s)
            i2 (make-intersection 2 s)
            intersections [i2 i1]
            hit (find-hit intersections)]
        (is (= hit i1))))
    (testing "when some intersections have negative t"
      (let [i1 (make-intersection -1 s)
            i2 (make-intersection 1 s)
            intersections [i2 i1]
            hit (find-hit intersections)]
        (is (= hit i2))))
    (testing "when all intersections have negative t"
      (let [i1 (make-intersection -2 s)
            i2 (make-intersection -1 s)
            intersections [i2 i1]
            hit (find-hit intersections)]
        (is (nil? hit))))
    (testing "hit is intersection with lowest non-negative t"
      (let [i1 (make-intersection 5 s)
            i2 (make-intersection 7 s)
            i3 (make-intersection -3 s)
            i4 (make-intersection 2 s)
            intersections [i1 i2 i3 i4]
            hit (find-hit intersections)]
        (is (= hit i4))))))

(deftest testing-translate
  (testing "translating a ray has no effect on its direction vector"
    (let [ray (make-ray [1 2 3 1] [0 1 0 0])
          {:keys [point direction]} (make-ray [4 6 8 1] [0 1 0 0])]
      (is (≈ point (:point (translate ray 3 4 5))))
      (is (≈ direction (:direction (translate ray 3 4 5)))))))

(deftest testing-scale
  (testing "scaling a ray transforms both its point and direction vector"
    (let [ray (make-ray [1 2 3 1] [0 1 0 0])
          {:keys [point direction]} (make-ray [2 6 12 1] [0 3 0 0])
          scaled-ray (scale ray 2 3 4)]
      (is (≈ point (:point scaled-ray )))
      (is (≈ direction (:direction scaled-ray ))))))
