(ns scintilla.shapes-test
  (:require [clojure.test :refer :all]
            [scintilla.numeric :refer [≈]]
            [scintilla.ray :as r]
            [scintilla.shapes :refer :all]
            [scintilla.transformation :as t]))

(deftest testing-intersections-for-sphere
  (testing "a ray that intersects a sphere at two points"
    (let [ray    (r/make-ray [0 0 -5 1] [0 0 1 0])
          sphere (make-sphere [1.0 0.0 0.0])
          points (intersections-for sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [4.0 6.0] (mapv :t points)))))
  (testing "a ray that intersects a sphere at one point"
    (let [ray    (r/make-ray [0 1 -5 1] [0 0 1 0])
          sphere (make-sphere [1.0 0.0 0.0])
          points (intersections-for sphere ray)]
      (is (= 1 (count points)))
      (is (≈ [5.0] (mapv :t points)))))
  (testing "a ray that intersects a sphere at one point"
    (let [ray    (r/make-ray [0 2 -5 1] [0 0 1 0])
          sphere (make-sphere [1.0 0.0 0.0])
          points (intersections-for sphere ray)]
      (is (= 0 (count points)))))
  (testing "a ray that originates from within a sphere"
    (let [ray    (r/make-ray [0 0 0 1] [0 0 1 0])
          sphere (make-sphere [1.0 0.0 0.0])
          points (intersections-for sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [-1.0 1.0] (mapv :t points)))))
  (testing "a ray that originates in front of a sphere"
    (let [ray    (r/make-ray [0 0 5 1] [0 0 1 0])
          sphere (make-sphere [1.0 0.0 0.0])
          points (intersections-for sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [-6.0 -4.0] (mapv :t points)))))
  (testing "intersecting a scaled sphere with a ray"
    (let [ray           (r/make-ray [0 0 -5 1] [0 0 1 0])
          S             (t/scaling-matrix 2 2 2)
          sphere        (make-sphere [1.0 0.0 0.0] S)
          intersections (intersections-for sphere ray)]
      (is (= 2 (count intersections)))
      (is (≈ [3.0 7.0] (mapv :t intersections)))))
  (testing "intersecting a translated sphere with a ray"
    (let [ray           (r/make-ray [0 0 -5 1] [0 0 1 0])
          T             (t/translation-matrix 5 0 0)
          sphere        (make-sphere [1.0 0.0 0.0] T)
          intersections (intersections-for sphere ray)]
      (is (= 0 (count intersections))))))

(deftest testing-find-intersections-for-plane
  (testing "a ray intersecting an xz plane from above"
    (let [plane             (make-plane)
          ray               (r/make-ray [0 1 0 1] [0 -1 0 0])
          intersections     (intersections-for plane ray)
          {:keys [t shape]} (first intersections)]
      (is (= 1 (count intersections)))
      (is (≈ 1 t))
      (is (= plane shape))))
  (testing "a ray intersecting an xz plane from below"
    (let [plane             (make-plane)
          ray               (r/make-ray [0 -1 0 1] [0 1 0 0])
          intersections     (intersections-for plane ray)
          {:keys [t shape]} (first intersections)]
      (is (= 1 (count intersections)))
      (is (≈ 1 t))
      (is (= plane shape))))
  (testing "a ray parallel to the xz plane"
    (let [plane             (make-plane)
          ray               (r/make-ray [0 10 0 1] [0 0 1 0])
          intersections     (intersections-for plane ray)
          {:keys [t shape]} (first intersections)]
      (is (= 0 (count intersections)))))
  (testing "a ray that lies in the xz plane"
    (let [plane             (make-plane)
          ray               (r/make-ray [0 0 0 1] [0 0 1 0])
          intersections     (intersections-for plane ray)
          {:keys [t shape]} (first intersections)]
      (is (= 0 (count intersections))))))

(deftest testing-normal-for-sphere
  (testing "the normal on a sphere at a point on the x axis"
    (let [sphere (make-sphere)
          point  [1 0 0 1]
          expected-value [1 0 0 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a sphere at a point on the y axis"
    (let [sphere (make-sphere)
          point  [0 1 0 1]
          expected-value [0 1 0 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a sphere at a point on the z axis"
    (let [sphere (make-sphere)
          point  [0 0 1 1]
          expected-value [0 0 1 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a sphere at a non-axial point"
    (let [sphere         (make-sphere)
          √3⟋3           (/ (Math/sqrt 3) 3.0)
          point          [√3⟋3 √3⟋3 √3⟋3 1]
          expected-value [√3⟋3 √3⟋3 √3⟋3 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a translated sphere"
    (let [transform      (t/translation-matrix 0 1 0)
          sphere         (make-sphere [1 0 0] transform)
          point          [0 1.70711 -0.70711 1]
          expected-value [0 0.70711 -0.70711 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a scaled sphere"
    (let [transform      (t/scaling-matrix 1.0 0.5 1.0)
          sphere         (make-sphere [1 0 0] transform)
          point          [0 0.70711 -0.70711 1]
          expected-value [0 0.97014 -0.24254 0]]
      (is (≈ expected-value (normal-for sphere point))))))

(deftest testing-normal-for-plane
  (testing "the normal on any point of a plane"
    (let [plane          (make-plane)
          points         [[0 0 0 1] [10 0 -10 1] [-5 0 150 1]]
          expected-value [0 1 0 0]]
      (is (every? #(≈ expected-value %) (map #(normal-for plane %) points))))))