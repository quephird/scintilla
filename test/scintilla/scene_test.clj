(ns scintilla.scene-test
  (:require [clojure.test :refer :all]
            [scintilla.lighting :as l]
            [scintilla.materials :as a]
            [scintilla.numeric :refer [≈]]
            [scintilla.ray :as r]
            [scintilla.scene :refer :all]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

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
          world         (make-scene [sphere1 sphere2] l/default-light)
          ray           (r/make-ray [0 0 -5 1] [0 0 1 0])
          intersections (find-all-intersections world ray)]
      (is (= 4 (count intersections)))
      (is (≈ [4.0 4.5 5.5 6.0] (set (mapv :t intersections)))))))

(deftest testing-find-hit
  (testing "when all intersections have positive t"
    (let [s  (s/make-sphere [1.0 0.0 0.0])
          i1 (s/make-intersection 1 s)
          i2 (s/make-intersection 2 s)
          intersections [i2 i1]
          hit (find-hit intersections)]
      (is (= hit i1))))
  (testing "when some intersections have negative t"
    (let [s  (s/make-sphere [1.0 0.0 0.0])
          i1 (s/make-intersection -1 s)
          i2 (s/make-intersection 1 s)
          intersections [i2 i1]
          hit (find-hit intersections)]
      (is (= hit i2))))
  (testing "when all intersections have negative t"
    (let [s  (s/make-sphere [1.0 0.0 0.0])
          i1 (s/make-intersection -2 s)
          i2 (s/make-intersection -1 s)
          intersections [i2 i1]
          hit (find-hit intersections)]
      (is (nil? hit))))
  (testing "hit is intersection with lowest non-negative t"
    (let [s  (s/make-sphere [1.0 0.0 0.0])
          i1 (s/make-intersection 5 s)
          i2 (s/make-intersection 7 s)
          i3 (s/make-intersection -3 s)
          i4 (s/make-intersection 2 s)
          intersections [i1 i2 i3 i4]
          hit (find-hit intersections)]
      (is (= hit i4)))))

(deftest testing-make-prepared-hit
  (testing "precomputing the state of an intersection"
    (let [ray            (r/make-ray [0 0 -5 1] [0 0 1 0])
          sphere         (s/make-sphere)
          intersections  (s/find-intersections sphere ray)
          hit            (find-hit intersections)
          prepared-hit   (make-prepared-hit hit ray intersections)]
      (is (≈ [0 0 -1 1] (:surface-point prepared-hit)))
      (is (≈ [0 0 -1 0] (:surface-normal prepared-hit)))
      (is (≈ [0 0 -1 0] (:eye-direction prepared-hit)))
      (is (= false (:inside prepared-hit)))))
  (testing "precomputing the state of an intersection"
    (let [ray            (r/make-ray [0 0 0 1] [0 0 1 0])
          sphere         (s/make-sphere)
          intersections  (s/find-intersections sphere ray)
          hit            (find-hit intersections)
          prepared-hit   (make-prepared-hit hit ray intersections)]
      (is (≈ [0 0 1 1]  (:surface-point prepared-hit)))
      (is (≈ [0 0 -1 0] (:surface-normal prepared-hit)))
      (is (≈ [0 0 -1 0] (:eye-direction prepared-hit)))
      (is (= true (:inside prepared-hit)))))
  (testing "precomputing the reflection vector"
    (let [plane          (s/make-plane)
          ray            (r/make-ray [0 1 -1 1] [0 -0.7071 0.7071 0])
          intersections  (s/find-intersections plane ray)
          hit            (find-hit intersections)
          prepared-hit   (make-prepared-hit hit ray intersections)
          expected-value [0.0 0.7071 0.7071 0]]
      (is (≈ expected-value (:reflected-vector prepared-hit)))))
  (testing "finding refractive indices at various intersections"
    ;; This is a scene with three spheres, A, B, and C, with a
    ;; ray that originates from the left of A and goes to the
    ;; right, intersecting six times, with points number 0-5 below.
    ;;
    ;;                            ,-‾‾‾‾‾‾‾‾‾‾-,
    ;;                          ⟋       A       ⟍
    ;;                        ⟋   ______  ______  ⟍
    ;;                       /  ⟋   B   ⟋⟍   C   ⟍  \
    ;;              ________|__/______ /___\_______\__|________
    ;;                     0| 1\      2\   /3      /4 |5
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

          scene          (make-scene [sphere-a sphere-b sphere-c] l/default-light)
          ray            (r/make-ray [0 0 -4 1] [0 0 1 0])
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
