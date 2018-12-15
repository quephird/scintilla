(ns scintilla.ray-test
  (:require [clojure.test :refer :all]
            [scintilla.camera :as c]
            [scintilla.lighting :as l]
            [scintilla.materials :as a]
            [scintilla.matrix :as m]
            [scintilla.numeric :refer :all]
            [scintilla.ray :refer :all]
            [scintilla.scene :as e]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]
            [scintilla.numeric :refer [≈]]))

(deftest testing-position
  (testing "computing the position of the end of a ray for different values of t"
    (let [ray (make-ray [2 3 4 1] [1 0 0 0])]
      (is (≈ [2 3 4 1] (position ray 0)))
      (is (≈ [3 3 4 1] (position ray 1)))
      (is (≈ [1 3 4 1] (position ray -1)))
      (is (≈ [4.5 3 4 1] (position ray 2.5))))))

(deftest testing-find-intersections-for-sphere
  (testing "a ray that intersects a sphere at two points"
    (let [ray    (make-ray [0 0 -5 1] [0 0 1 0])
          sphere (s/make-sphere [1.0 0.0 0.0])
          points (find-intersections sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [4.0 6.0] (mapv :t points)))))
  (testing "a ray that intersects a sphere at one point"
    (let [ray    (make-ray [0 1 -5 1] [0 0 1 0])
          sphere (s/make-sphere [1.0 0.0 0.0])
          points (find-intersections sphere ray)]
      (is (= 1 (count points)))
      (is (≈ [5.0] (mapv :t points)))))
  (testing "a ray that intersects a sphere at one point"
    (let [ray    (make-ray [0 2 -5 1] [0 0 1 0])
          sphere (s/make-sphere [1.0 0.0 0.0])
          points (find-intersections sphere ray)]
      (is (= 0 (count points)))))
  (testing "a ray that originates from within a sphere"
    (let [ray    (make-ray [0 0 0 1] [0 0 1 0])
          sphere (s/make-sphere [1.0 0.0 0.0])
          points (find-intersections sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [-1.0 1.0] (mapv :t points)))))
  (testing "a ray that originates in front of a sphere"
    (let [ray    (make-ray [0 0 5 1] [0 0 1 0])
          sphere (s/make-sphere [1.0 0.0 0.0])
          points (find-intersections sphere ray)]
      (is (= 2 (count points)))
      (is (≈ [-6.0 -4.0] (mapv :t points)))))
  (testing "intersecting a scaled sphere with a ray"
    (let [ray           (make-ray [0 0 -5 1] [0 0 1 0])
          S             (t/scaling-matrix 2 2 2)
          sphere        (s/make-sphere [1.0 0.0 0.0] S)
          intersections (find-intersections sphere ray)]
      (is (= 2 (count intersections)))
      (is (≈ [3.0 7.0] (mapv :t intersections)))))
  (testing "intersecting a translated sphere with a ray"
    (let [ray           (make-ray [0 0 -5 1] [0 0 1 0])
          T             (t/translation-matrix 5 0 0)
          sphere        (s/make-sphere [1.0 0.0 0.0] T)
          intersections (find-intersections sphere ray)]
      (is (= 0 (count intersections))))))

(deftest testing-find-intersections-for-plane
  (testing "a ray intersecting an xz plane from above"
    (let [plane             (s/make-plane)
          ray               (make-ray [0 1 0 1] [0 -1 0 0])
          intersections     (find-intersections plane ray)
          {:keys [t shape]} (first intersections)]
      (is (= 1 (count intersections)))
      (is (≈ 1 t))
      (is (= plane shape))))
  (testing "a ray intersecting an xz plane from below"
    (let [plane             (s/make-plane)
          ray               (make-ray [0 -1 0 1] [0 1 0 0])
          intersections     (find-intersections plane ray)
          {:keys [t shape]} (first intersections)]
      (is (= 1 (count intersections)))
      (is (≈ 1 t))
      (is (= plane shape))))
  (testing "a ray parallel to the xz plane"
    (let [plane             (s/make-plane)
          ray               (make-ray [0 10 0 1] [0 0 1 0])
          intersections     (find-intersections plane ray)
          {:keys [t shape]} (first intersections)]
      (is (= 0 (count intersections)))))
  (testing "a ray that lies in the xz plane"
    (let [plane             (s/make-plane)
          ray               (make-ray [0 0 0 1] [0 0 1 0])
          intersections     (find-intersections plane ray)
          {:keys [t shape]} (first intersections)]
      (is (= 0 (count intersections))))))

(deftest testing-find-all-intersections
  (testing "a ray that intersects a sphere at two points"
    (let [material1     (a/make-material {:color [0.8 1.0 0.6]
                                          :ambient 0.1
                                          :diffuse 0.7
                                          :specular 0.2
                                          :shininess 200
                                          :pattern nil})
          sphere1       (s/make-sphere material1)
          transform2    (t/scaling-matrix 0.5 0.5 0.5)
          sphere2       (s/make-sphere a/default-material transform2)
          world         (e/add-objects (e/make-scene) [sphere1 sphere2])
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
    (let [s  (s/make-sphere [1.0 0.0 0.0])
          i1 (make-intersection 1 s)
          i2 (make-intersection 2 s)
          intersections [i2 i1]
          hit (find-hit intersections)]
      (is (= hit i1))))
  (testing "when some intersections have negative t"
    (let [s  (s/make-sphere [1.0 0.0 0.0])
          i1 (make-intersection -1 s)
          i2 (make-intersection 1 s)
          intersections [i2 i1]
          hit (find-hit intersections)]
      (is (= hit i2))))
  (testing "when all intersections have negative t"
    (let [s  (s/make-sphere [1.0 0.0 0.0])
          i1 (make-intersection -2 s)
          i2 (make-intersection -1 s)
          intersections [i2 i1]
          hit (find-hit intersections)]
      (is (nil? hit))))
  (testing "hit is intersection with lowest non-negative t"
    (let [s  (s/make-sphere [1.0 0.0 0.0])
          i1 (make-intersection 5 s)
          i2 (make-intersection 7 s)
          i3 (make-intersection -3 s)
          i4 (make-intersection 2 s)
          intersections [i1 i2 i3 i4]
          hit (find-hit intersections)]
      (is (= hit i4)))))

(deftest testing-ray-for
  (testing "constructing a ray through the center of the canvas"
    (let [camera (c/make-camera 201 101 π⟋2)
          ray    (ray-for camera 100 50)]
      (is (≈ [0 0 0 1] (:point ray)))
      (is (≈ [0 0 -1 0] (:direction ray)))))
  (testing "constructing a ray through the corner of the canvas"
    (let [camera (c/make-camera 201 101 π⟋2)
          ray    (ray-for camera 0 0)]
      (is (≈ [0 0 0 1] (:point ray)))
      (is (≈ [0.66519 0.33259 -0.66851] (:direction ray)))))
  (testing "constructing a ray through the center of the canvas"
    (let [R         (t/rotation-y-matrix π⟋4)
          T         (t/translation-matrix 0 -2 5)
          transform (m/matrix-times R T)
          camera    (c/make-camera 201 101 π⟋2 transform)
          ray       (ray-for camera 100 50)]
      (is (≈ [0 2 -5 1] (:point ray)))
      (is (≈ [0.70711 0 -0.70711 0] (:direction ray))))))

(deftest testing-normal-for-sphere
  (testing "the normal on a sphere at a point on the x axis"
    (let [sphere (s/make-sphere)
          point  [1 0 0 1]
          expected-value [1 0 0 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a sphere at a point on the y axis"
    (let [sphere (s/make-sphere)
          point  [0 1 0 1]
          expected-value [0 1 0 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a sphere at a point on the z axis"
    (let [sphere (s/make-sphere)
          point  [0 0 1 1]
          expected-value [0 0 1 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a sphere at a non-axial point"
    (let [sphere         (s/make-sphere)
          √3⟋3           (/ (Math/sqrt 3) 3.0)
          point          [√3⟋3 √3⟋3 √3⟋3 1]
          expected-value [√3⟋3 √3⟋3 √3⟋3 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a translated sphere"
    (let [transform      (t/translation-matrix 0 1 0)
          sphere         (s/make-sphere [1 0 0] transform)
          point          [0 1.70711 -0.70711 1]
          expected-value [0 0.70711 -0.70711 0]]
      (is (≈ expected-value (normal-for sphere point)))))
  (testing "the normal on a scaled sphere"
    (let [transform      (t/scaling-matrix 1.0 0.5 1.0)
          sphere         (s/make-sphere [1 0 0] transform)
          point          [0 0.70711 -0.70711 1]
          expected-value [0 0.97014 -0.24254 0]]
      (is (≈ expected-value (normal-for sphere point))))))

(deftest testing-normal-for-plane
  (testing "the normal on any point of a plane"
    (let [plane          (s/make-plane)
          points         [[0 0 0 1] [10 0 -10 1] [-5 0 150 1]]
          expected-value [0 1 0 0]]
      (is (every? #(≈ expected-value %) (map #(normal-for plane %) points))))))

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

(deftest testing-make-prepared-hit
  (testing "precomputing the state of an intersection"
    (let [ray            (make-ray [0 0 -5 1] [0 0 1 0])
          sphere         (s/make-sphere)
          intersections  (find-intersections sphere ray)
          hit            (find-hit intersections)
          prepared-hit   (make-prepared-hit hit ray intersections)]
      (is (≈ [0 0 -1 1] (:surface-point prepared-hit)))
      (is (≈ [0 0 -1 0] (:surface-normal prepared-hit)))
      (is (≈ [0 0 -1 0] (:eye-direction prepared-hit)))
      (is (= false (:inside prepared-hit)))))
  (testing "precomputing the state of an intersection"
    (let [ray            (make-ray [0 0 0 1] [0 0 1 0])
          sphere         (s/make-sphere)
          intersections  (find-intersections sphere ray)
          hit            (find-hit intersections)
          prepared-hit   (make-prepared-hit hit ray intersections)]
      (is (≈ [0 0 1 1]  (:surface-point prepared-hit)))
      (is (≈ [0 0 -1 0] (:surface-normal prepared-hit)))
      (is (≈ [0 0 -1 0] (:eye-direction prepared-hit)))
      (is (= true (:inside prepared-hit)))))
  (testing "precomputing the reflection vector"
    (let [plane          (s/make-plane)
          ray            (make-ray [0 1 -1 1] [0 -0.7071 0.7071 0])
          intersections  (find-intersections plane ray)
          hit            (find-hit intersections)
          prepared-hit   (make-prepared-hit hit ray intersections)
          expected-value [0.0 0.7071 0.7071 0]]
      (is (≈ expected-value (:reflected-vector prepared-hit)))))
  (testing "finding refractive indices at various intersections"
    ;;
    ;;                            ,-‾‾‾‾‾‾‾‾‾‾-,
    ;;                          ⟋       A       ⟍
    ;;                        ⟋   ______  ______  ⟍
    ;;                       /  ⟋   B   ⟋⟍   C   ⟍  \
    ;;              ________|__/______ /__\_______\__|________
    ;;                     0| 1\      2\  /3      /4 |5
    ;;                       \  ⟍       ⟍⟋       ⟋  /
    ;;                         ⟍  ‾‾‾‾‾‾  ‾‾‾‾‾‾  ⟋
    ;;                           ⟍              ⟋
    ;;                             '-________ -'
    ;;
    (let [material-a     (a/make-material {:refractive-index 1.5})
          transform-a    (t/scaling-matrix 2 2 2)
          sphere-a       (s/make-sphere material-a transform-a)

          material-b     (a/make-material {:refractive-index 2.0})
          transform-b    (t/translation-matrix 0 0 -0.25)
          sphere-b       (s/make-sphere material-b transform-b)

          material-c     (a/make-material {:refractive-index 2.5})
          transform-c    (t/translation-matrix 0 0 0.25)
          sphere-c       (s/make-sphere material-c transform-c)

          scene          (e/make-scene [sphere-a sphere-b sphere-c] l/default-light)
          ray            (make-ray [0 0 -4 1] [0 0 1 0])
          intersections  (find-all-intersections scene ray)

          expected-values [[1.0 1.5]   ;; n1 and n2 values for intersection 0
                           [1.5 2.0]   ;; "  "   "  "      "   "            1
                           [2.0 2.5]   ;; "  "   "  "      "   "            2
                           [2.5 2.5]   ;; "  "   "  "      "   "            3
                           [2.5 1.5]   ;; "  "   "  "      "   "            4
                           [1.5 1.0]]] ;; "  "   "  "      "   "            5
      (is (= expected-values
             (->> intersections
                  (map #(make-prepared-hit % ray intersections))
                  (map #(map % [:n1 :n2]))))))))
