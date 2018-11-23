(ns scintilla.ray-test
  (:require [clojure.test :refer :all]
            [scintilla.lighting :as l]
            [scintilla.materials :as a]
            [scintilla.matrix :as m]
            [scintilla.ray :refer :all]
            [scintilla.scene :as s]
            [scintilla.shapes :refer :all]
            [scintilla.transformation :as t]
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
          sphere (make-sphere [1.0 0.0 0.0])
          points (find-intersections sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [4.0 6.0] (mapv :t points)))))
  (testing "a ray that intersects a sphere at one point"
    (let [ray    (make-ray [0 1 -5 1] [0 0 1 0])
          sphere (make-sphere [1.0 0.0 0.0])
          points (find-intersections sphere ray)]
      (is (= 1 (count points)))
      (is (≈ [5.0] (mapv :t points)))))
  (testing "a ray that intersects a sphere at one point"
    (let [ray    (make-ray [0 2 -5 1] [0 0 1 0])
          sphere (make-sphere [1.0 0.0 0.0])
          points (find-intersections sphere ray)]
      (is (= 0 (count points)))))
  (testing "a ray that originates from within a sphere"
    (let [ray    (make-ray [0 0 0 1] [0 0 1 0])
          sphere (make-sphere [1.0 0.0 0.0])
          points (find-intersections sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [-1.0 1.0] (mapv :t points)))))
  (testing "a ray that originates in front of a sphere"
    (let [ray    (make-ray [0 0 5 1] [0 0 1 0])
          sphere (make-sphere [1.0 0.0 0.0])
          points (find-intersections sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [-6.0 -4.0] (mapv :t points)))))
  (testing "intersecting a scaled sphere with a ray"
    (let [ray           (make-ray [0 0 -5 1] [0 0 1 0])
          S             (t/scaling-matrix 2 2 2)
          sphere        (make-sphere [1.0 0.0 0.0] S)
          intersections (find-intersections sphere ray)]
      (is (= 2 (count intersections)))
      (is (≈ [3.0 7.0] (mapv :t intersections)))))
  (testing "intersecting a translated sphere with a ray"
    (let [ray           (make-ray [0 0 -5 1] [0 0 1 0])
          T             (t/translation-matrix 5 0 0)
          sphere        (make-sphere [1.0 0.0 0.0] T)
          intersections (find-intersections sphere ray)]
      (is (= 0 (count intersections))))))

(deftest testing-find-all-intersections
  (testing "a ray that intersects a sphere at two points"
    (let [material1     (a/make-material [0.8 1.0 0.6] 0.1 0.7 0.2 200)
          sphere1       (make-sphere material1)
          transform2    (t/scaling-matrix 0.5 0.5 0.5)
          sphere2       (make-sphere a/default-material transform2)
          world         (s/add-objects (s/make-scene) [sphere1 sphere2])
          ray           (make-ray [0 0 -5 1] [0 0 1 0])
          intersections (find-all-intersections world ray)]
      (is (= 4 (count intersections)))
      (is (≈ [4.0 4.5 5.5 6.0] (set (mapv :t intersections)))))))

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

(deftest testing-find-hit
  (testing "when all intersections have positive t"
    (let [s (make-sphere [1.0 0.0 0.0])
          i1 (make-intersection 1 s)
          i2 (make-intersection 2 s)
          intersections [i2 i1]
          hit (find-hit intersections)]
      (is (= hit i1))))
  (testing "when some intersections have negative t"
    (let [s (make-sphere [1.0 0.0 0.0])
          i1 (make-intersection -1 s)
          i2 (make-intersection 1 s)
          intersections [i2 i1]
          hit (find-hit intersections)]
      (is (= hit i2))))
  (testing "when all intersections have negative t"
    (let [s (make-sphere [1.0 0.0 0.0])
          i1 (make-intersection -2 s)
          i2 (make-intersection -1 s)
          intersections [i2 i1]
          hit (find-hit intersections)]
      (is (nil? hit))))
  (testing "hit is intersection with lowest non-negative t"
    (let [s (make-sphere [1.0 0.0 0.0])
          i1 (make-intersection 5 s)
          i2 (make-intersection 7 s)
          i3 (make-intersection -3 s)
          i4 (make-intersection 2 s)
          intersections [i1 i2 i3 i4]
          hit (find-hit intersections)]
      (is (= hit i4)))))

(deftest testing-make-prepared-hit
  (testing "precomputing the state of an intersection"
    (let [ray            (make-ray [0 0 -5 1] [0 0 1 0])
          sphere         (make-sphere)
          hit            (find-hit (find-intersections sphere ray))
          prepared-hit   (make-prepared-hit hit ray)]
      (is (≈ [0 0 -1 1] (:surface-point prepared-hit)))
      (is (≈ [0 0 -1 0] (:surface-normal prepared-hit)))
      (is (≈ [0 0 -1 0] (:eye-direction prepared-hit)))
      (is (= false (:inside prepared-hit)))))
  (testing "precomputing the state of an intersection"
    (let [ray            (make-ray [0 0 0 1] [0 0 1 0])
          sphere         (make-sphere)
          hit            (find-hit (find-intersections sphere ray))
          prepared-hit   (make-prepared-hit hit ray)]
      (is (≈ [0 0 1 1] (:surface-point prepared-hit)))
      (is (≈ [0 0 -1 0] (:surface-normal prepared-hit)))
      (is (≈ [0 0 -1 0] (:eye-direction prepared-hit)))
      (is (= true (:inside prepared-hit))))))
