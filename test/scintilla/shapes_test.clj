(ns scintilla.shapes-test
  (:require [clojure.test :refer :all]
            [scintilla.numeric :refer [≈]]
            [scintilla.ray :as r]
            [scintilla.shapes :refer :all]
            [scintilla.transformation :as t]
            [scintilla.tuple :as u]))

(deftest testing-intersections-for-sphere
  (testing "a ray that intersects a sphere at two points"
    (let [ray    (r/make-ray [0 0 -5 1] [0 0 1 0])
          sphere (make-sphere)
          points (intersections-for sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [4.0 6.0] (mapv :t points)))))
  (testing "a ray that intersects a sphere at one point"
    (let [ray    (r/make-ray [0 1 -5 1] [0 0 1 0])
          sphere (make-sphere)
          points (intersections-for sphere ray)]
      (is (= 1 (count points)))
      (is (≈ [5.0] (mapv :t points)))))
  (testing "a ray that intersects a sphere at one point"
    (let [ray    (r/make-ray [0 2 -5 1] [0 0 1 0])
          sphere (make-sphere)
          points (intersections-for sphere ray)]
      (is (= 0 (count points)))))
  (testing "a ray that originates from within a sphere"
    (let [ray    (r/make-ray [0 0 0 1] [0 0 1 0])
          sphere (make-sphere)
          points (intersections-for sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [-1.0 1.0] (mapv :t points)))))
  (testing "a ray that originates in front of a sphere"
    (let [ray    (r/make-ray [0 0 5 1] [0 0 1 0])
          sphere (make-sphere)
          points (intersections-for sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [-6.0 -4.0] (mapv :t points)))))
  (testing "intersecting a scaled sphere with a ray"
    (let [ray           (r/make-ray [0 0 -5 1] [0 0 1 0])
          S             (t/scaling-matrix 2 2 2)
          sphere        (make-sphere {:transform S})
          intersections (intersections-for sphere ray)]
      (is (= 2 (count intersections)))
      (is (≈ [3.0 7.0] (mapv :t intersections)))))
  (testing "intersecting a translated sphere with a ray"
    (let [ray           (r/make-ray [0 0 -5 1] [0 0 1 0])
          T             (t/translation-matrix 5 0 0)
          sphere        (make-sphere {:transform T})
          intersections (intersections-for sphere ray)]
      (is (= 0 (count intersections))))))

(deftest testing-intersections-for-plane
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

(deftest testing-intersections-for-cube
  (testing "rays intersecting the cube twice"
    (let [cube       (make-cube)
          points     [[ 5.0  0.5  0.0 1]
                      [-5.0  0.5  0.0 1]
                      [ 0.5  5.0  0.0 1]
                      [ 0.5 -5.0  0.0 1]
                      [ 0.5  0.0  5.0 1]
                      [ 0.5  0.0 -5.0 1]
                      [ 0.0  0.5  0.0 1]]
          directions [[-1  0  0 0]
                      [ 1  0  0 0]
                      [ 0 -1  0 0]
                      [ 0  1  0 0]
                      [ 0  0 -1 0]
                      [ 0  0  1 0]
                      [ 0  0  1 0]]
          rays       (map #(r/make-ray %1 %2) points directions)
          expected-values [[4 6]
                           [4 6]
                           [4 6]
                           [4 6]
                           [4 6]
                           [4 6]
                           [-1 1]]
          actual-values (->> rays
                             (map #(intersections-for cube %))
                             (map #(map :t %)))]
      (is (≈ expected-values actual-values))))
  (testing "rays missing the cube"
    (let [cube       (make-cube)
          points     [[-2.0  0.0  0.0 1]
                      [ 0.0 -2.0  0.0 1]
                      [ 0.0  0.0 -2.0 1]
                      [ 2.0  0.0  2.0 1]
                      [ 0.0  2.0  2.0 1]
                      [ 2.0  2.0  0.0 1]]
          directions [[ 0.2673  0.5345  0.8018 0]
                      [ 0.8018  0.2673  0.5345 0]
                      [ 0.5345  0.8018  0.2673 0]
                      [ 0.0     0.0    -1.0    0]
                      [ 0.0    -1.0     0.0    0]
                      [-1.0     0.0     0.0    0]]
          rays       (map #(r/make-ray %1 %2) points directions)]
      (is (every? empty? (map #(intersections-for cube %) rays))))))

(deftest testing-intersections-for-cylinder
  (testing "rays missing a cylinder"
    (let [cylinder   (make-cylinder)
          points     [[1 0 0 1] [0 0 0 1] [0 0 -5 0]]
          directions [[0 1 0 0] [0 1 0 0] [1 1 1 0]]
          rays       (map #(r/make-ray %1 (u/normalize %2)) points directions)]
      (is (every? empty? (map #(intersections-for cylinder %) rays)))))
  (testing "ray strikes a cylinder"
    (let [cylinder        (make-cylinder)
          points          [[1 0 -5 1] [0 0 -5 1] [0.5 0 -5 1]]
          directions      [[0 0 1 0] [0 0 1 0] [0.1 1 1 0]]
          rays            (map #(r/make-ray %1 (u/normalize %2)) points directions)
          expected-values [[5] [4 6] [6.80798 7.08872]]]
      (is (≈ expected-values (->> rays
                               (map #(intersections-for cylinder %))
                               (map #(map :t %)))))))
  (testing "intersecting a constrained cylinder"
    (let [cylinder        (make-cylinder {:minimum 1
                                          :maximum 2})
          points          [[0 1.5 0 1]
                           [0 3 -5 1]
                           [0 0 -5 1]
                           [0 2 -5 1]
                           [0 1 -5 1]
                           [0 1.5 -2 1]]
          directions      [[0.1 1 0 0]
                           [0 0 1 0]
                           [0 0 1 0]
                           [0 0 1 0]
                           [0 0 1 0]
                           [0 0 1 0]]
          rays            (map #(r/make-ray %1 (u/normalize %2)) points directions)
          expected-counts [0 0 0 0 0 2]]
      (is (≈ expected-counts (->> rays
                                  (map #(intersections-for cylinder %))
                                  (map count))))))
  (testing "intersecting the caps of a closed cylinder"
    (let [cylinder        (make-cylinder {:minimum 1
                                          :maximum 2
                                          :capped? true})
          points          [[0 3 0 1]
                           [0 3 -2 1]
                           [0 4 -2 1]
                           [0 0 -2 1]
                           [0 -1 -2 1]]
          directions      [[0 -1 0 0]
                           [0 -1 2 0]
                           [0 -1 1 0]
                           [0 1 2 0]
                           [0 1 1 0]]
          rays            (map #(r/make-ray %1 (u/normalize %2)) points directions)
          expected-counts [2 2 2 2 2]]
      (is (≈ expected-counts (->> rays
                                  (map #(intersections-for cylinder %))
                                  (map count)))))))

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
          sphere         (make-sphere {:transform transform})
          point          [0 1.70711 -0.70711 1]
          expected-value [0 0.70711 -0.70711 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a scaled sphere"
    (let [transform      (t/scaling-matrix 1.0 0.5 1.0)
          sphere         (make-sphere {:transform transform})
          point          [0 0.70711 -0.70711 1]
          expected-value [0 0.97014 -0.24254 0]]
      (is (≈ expected-value (normal-for sphere point))))))

(deftest testing-normal-for-plane
  (testing "the normal on any point of a plane"
    (let [plane          (make-plane)
          points         [[0 0 0 1] [10 0 -10 1] [-5 0 150 1]]
          expected-value [0 1 0 0]]
      (is (every? #(≈ expected-value %) (map #(normal-for plane %) points))))))

(deftest testing-normal-for-cube
  (testing "points on various faces of a default cube"
    (let [cube   (make-cube)
          points [[ 1    0.5  -0.8]
                  [-1   -0.2   0.9]
                  [-0.4  1    -0.1]
                  [ 0.3 -1    -0.7]
                  [-0.6  0.3   1]
                  [ 0.4  0.4  -1]
                  [ 1    1     1]
                  [-1   -1    -1]]
          expected-values [[ 1  0  0]
                           [-1  0  0]
                           [ 0  1  0]
                           [ 0 -1  0]
                           [ 0  0  1]
                           [ 0  0 -1]
                           [ 1  0  0]
                           [-1  0  0]]]
      (is (≈ expected-values (map #(normal-for cube %) points))))))

(deftest testing-normal-for-cylinder
  (testing "Normal vector on a cylinder"
    (let [cylinder        (make-cylinder)
          points          [[1 0 0 1] [0 5 -1 1] [0 -2 1 1] [-1 1 0 1]]
          expected-values [[1 0 0 0] [0 0 -1 0] [0 0 1 0] [-1 0 0 0]]]
      (is (≈ expected-values (map #(normal-for cylinder %) points))))))
