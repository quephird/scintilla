(ns scintilla.lighting-test
  (:require [clojure.test :refer :all]
            [scintilla.materials :as a]
            [scintilla.lighting :refer :all]
            [scintilla.numeric :refer [≈]]
            [scintilla.ray :as r]
            [scintilla.scene :as e]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

(deftest testing-shadowed-by?
  (let [material1     (a/make-material [0.8 1.0 0.6] 0.1 0.7 0.2 200)
        sphere1       (s/make-sphere material1)
        transform2    (t/scaling-matrix 0.5 0.5 0.5)
        sphere2       (s/make-sphere a/default-material transform2)
        light         (make-light [-10 10 -10 1] [1 1 1])
        scene         (e/add-objects (e/make-scene) [sphere1 sphere2])]
    (testing "when nothing is collinear with point and light"
      ;;
      ;;              🔆          ❌
      ;;
      ;;                        ______
      ;;                      ⟋       ⟍
      ;;                     /          \
      ;;                     |          |
      ;;                      ⟍       ⟋
      ;;                        ‾‾‾‾‾‾
      ;;
      (is (false? (shadowed-by? scene [0 10 0 1]))))
    (testing "when object is between the point and the light"
      ;;
      ;;              🔆
      ;;
      ;;                        ______
      ;;                      ⟋       ⟍
      ;;                     /          \
      ;;                     |          |
      ;;                      ⟍       ⟋
      ;;                        ‾‾‾‾‾‾
      ;;                                     ❌
      ;;
      (is (true? (shadowed-by? scene [10 -10 10 1]))))
  (testing "when the object is behind the point"
      ;;
      ;;              🔆
      ;;
      ;;
      ;;                    ❌
      ;;                        ______
      ;;                      ⟋       ⟍
      ;;                     /          \
      ;;                     |          |
      ;;                      ⟍       ⟋
      ;;                        ‾‾‾‾‾‾
      ;;
      (is (false? (shadowed-by? scene [-2 -2 2 1]))))))

(deftest testing-lighting
  (testing "lighting with the eye between the light and the surface

                              |
                              |
                🔆   👁  <----|
                              |
                              |
    "
    (let [material       a/default-material
          surface-normal [0 0 -1 0]
          surface-point  [0 0 0 1]
          eye-direction  [0 0 -1 0]
          prepared-hit   {:shape
                           {:shape-type :sphere
                            :material material}
                          :surface-normal surface-normal
                          :surface-point surface-point
                          :eye-direction eye-direction}
          light          (make-light [0 0 -10 1] [1 1 1])]
      (is (≈ [0.1 0.1 0.1] (ambient light prepared-hit)))
      (is (≈ [0.9 0.9 0.9] (diffuse light prepared-hit)))
      (is (≈ [0.9 0.9 0.9] (specular light prepared-hit)))
      (is (≈ [1.9 1.9 1.9] (lighting light prepared-hit)))))
    (testing "lighting with the eye between light and surface, eye offset 45°

                           👁
                             ⟍  |
                               ⟍|
                  🔆       <----|
                                |
                                |
      "
      (let [material       a/default-material
            surface-normal [0 0 -1 0]
            surface-point  [0 0 0 1]
            eye-direction  [0 0.70711 -0.70711 0]
            prepared-hit   {:shape
                             {:shape-type :sphere
                              :material material}
                            :surface-normal surface-normal
                            :surface-point surface-point
                            :eye-direction eye-direction}
            light          (make-light [0 0 -10 1] [1 1 1])]
        (is (≈ [0.1 0.1 0.1] (ambient light prepared-hit)))
        (is (≈ [0.9 0.9 0.9] (diffuse light prepared-hit)))
        (is (≈ [0.0 0.0 0.0] (specular light prepared-hit)))
        (is (≈ [1.0 1.0 1.0] (lighting light prepared-hit)))))
    (testing "lighting with opposite surface, light offset 45°

                           🔆
                             ⟍  |
                               ⟍|
                  👁       <----|
                                |
                                |
      "
      (let [material       a/default-material
            surface-normal [0 0 -1 0]
            surface-point  [0 0 0 1]
            eye-direction  [0 0 -1 0]
            prepared-hit   {:shape
                             {:shape-type :sphere
                              :material material}
                            :surface-normal surface-normal
                            :surface-point surface-point
                            :eye-direction eye-direction}
            light          (make-light [0 10 -10 1] [1 1 1])]
        (is (≈ [0.1 0.1 0.1] (ambient light prepared-hit)))
        (is (≈ [0.6364 0.6364 0.6364] (diffuse light prepared-hit)))
        (is (≈ [0.0 0.0 0.0] (specular light prepared-hit)))
        (is (≈ [0.7364 0.7364 0.7364] (lighting light prepared-hit)))))
    (testing "lighting with eye in the path of the reflection vector

                           🔆
                             ⟍  |
                               ⟍|
                           <----|
                               ⟋|
                             ⟋  |
                           👁
      "
      (let [material       a/default-material
            surface-normal [0 0 -1 0]
            surface-point  [0 0 0 1]
            eye-direction  [0 (* -0.5 (Math/sqrt 2)) (* -0.5 (Math/sqrt 2)) 0]
            prepared-hit   {:shape
                             {:shape-type :sphere
                              :material material}
                            :surface-normal surface-normal
                            :surface-point surface-point
                            :eye-direction eye-direction}
            light          (make-light [0 10 -10 1] [1 1 1])]
        (is (≈ [0.1 0.1 0.1] (ambient light prepared-hit)))
        (is (≈ [0.6364 0.6364 0.6364] (diffuse light prepared-hit)))
        (is (≈ [0.9 0.9 0.9] (specular light prepared-hit)))
        (is (≈ [1.6364 1.6364 1.6364] (lighting light prepared-hit)))))
    (testing "lighting with the light behind the surface

                                |
                                |
                      👁   <----|----> 🔆
                                |
                                |
      "
      (let [material       a/default-material
            surface-normal [0 0 -1 0]
            surface-point  [0 0 0 1]
            eye-direction  [0 0 -1 0]
            prepared-hit   {:shape
                             {:shape-type :sphere
                              :material material}
                            :surface-normal surface-normal
                            :surface-point surface-point
                            :eye-direction eye-direction}
            light          (make-light [0 0 10 1] [1 1 1])]
        (is (≈ [0.1 0.1 0.1] (ambient light prepared-hit)))
        (is (≈ [0.0 0.0 0.0] (diffuse light prepared-hit)))
        (is (≈ [0.0 0.0 0.0] (specular light prepared-hit)))
        (is (≈ [0.1 0.1 0.1] (lighting light prepared-hit))))))


(deftest testing-color-for
  (testing "the color when a ray misses"
    (let [material1  (a/make-material [0.8 1.0 0.6] 0.1 0.7 0.2 200)
          sphere1    (s/make-sphere material1)
          transform2 (t/scaling-matrix 0.5 0.5 0.5)
          sphere2    (s/make-sphere a/default-material transform2)
          world      (e/add-objects (e/make-scene) [sphere1 sphere2])
          ray        (r/make-ray [0 0 -5 1] [0 1 0 0])]
      (is (≈ [0 0 0] (color-for world ray)))))
  (testing "the color when a ray hits"
    (let [material1  (a/make-material [0.8 1.0 0.6] 0.1 0.7 0.2 200)
          sphere1    (s/make-sphere material1)
          transform2 (t/scaling-matrix 0.5 0.5 0.5)
          sphere2    (s/make-sphere a/default-material transform2)
          world      (e/add-objects (e/make-scene) [sphere1 sphere2])
          ray        (r/make-ray [0 0 -5 1] [0 0 1 0])]
      (is (≈ [0.38066 0.47583 0.2855] (color-for world ray)))))
  (testing "the color with an intersection behind the ray"
    (let [material1  (a/make-material [0.8 1.0 0.6] 1.0 0.7 0.2 200)
          sphere1    (s/make-sphere material1)
          material2  (a/make-material [1 1 1] 1.0 0.9 0.9 200)
          transform2 (t/scaling-matrix 0.5 0.5 0.5)
          sphere2    (s/make-sphere material2 transform2)
          world      (e/add-objects (e/make-scene) [sphere1 sphere2])
          ray        (r/make-ray [0 0 0.75 1] [0 0 -1 0])]
      (is (≈ [1.0 1.0 1.0] (color-for world ray)))))

    ;   (testing "lighting with the surface in shadow"
    ;   Given eyev ← vector(0, 0, -1)
    ; And normalv ← vector(0, 0, -1)
    ; And light ← point_light(point(0, 0, -10), color(1, 1, 1)) And in_shadow ← true
    ; When result ← lighting(m, light, position, eyev, normalv, in_shadow) Then result = color(0.1, 0.1, 0.1)"))

      )
