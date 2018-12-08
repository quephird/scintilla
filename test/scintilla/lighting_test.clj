(ns scintilla.lighting-test
  (:require [clojure.test :refer :all]
            [scintilla.materials :as a]
            [scintilla.lighting :refer :all]
            [scintilla.numeric :refer [≈]]
            [scintilla.ray :as r]
            [scintilla.scene :as e]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

(deftest testing-shadowed?
  (let [material1     (a/make-material {:color [0.8 1.0 0.6]
                                        :ambient 0.1
                                        :diffuse 0.7
                                        :specular 0.2
                                        :shininess 200
                                        :pattern nil})
        sphere1       (s/make-sphere material1)
        transform2    (t/scaling-matrix 0.5 0.5 0.5)
        sphere2       (s/make-sphere a/default-material transform2)
        light         (make-light [-10 10 -10 1] [1 1 1])
        scene         (e/make-scene [sphere1 sphere2] light)]
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
      (is (false? (shadowed? scene [0 10 0 1]))))
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
      (is (true? (shadowed? scene [10 -10 10 1]))))
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
      (is (false? (shadowed? scene [-2 -2 2 1]))))))

(deftest testing-lighting
  (testing "lighting with the eye between the light and the surface"
    ;;
    ;;                            ______
    ;;                          ⟋       ⟍
    ;;                         /          \
    ;;            🔆   👁  <----|          |
    ;;                          ⟍        ⟋
    ;;                            ‾‾‾‾‾‾
    ;;
    (let [material       a/default-material
          transform      (t/translation-matrix 0 0 1)
          sphere         (s/make-sphere material transform)
          light          (make-light [0 0 -10 1] [1 1 1])
          scene          (e/make-scene [sphere] light)
          surface-normal [0 0 -1 0]
          surface-point  [0 0 0 1]
          eye-direction  [0 0 -1 0]
          prepared-hit   {:shape sphere
                          :surface-normal surface-normal
                          :surface-point surface-point
                          :eye-direction eye-direction}]
      (is (≈ [0.1 0.1 0.1] (ambient light prepared-hit)))
      (is (≈ [0.9 0.9 0.9] (diffuse light prepared-hit)))
      (is (≈ [0.9 0.9 0.9] (specular light prepared-hit)))
      (is (≈ [1.9 1.9 1.9] (lighting scene prepared-hit)))))
    (testing "lighting with the eye between light and surface, eye offset 45°"
      ;;
      ;;                    👁       _____
      ;;                      ⟍   ⟋       ⟍
      ;;                        ⟍/          \
      ;;            🔆       <----|          |
      ;;                          ⟍        ⟋
      ;;                             ‾‾‾‾‾
      ;;
      (let [material       a/default-material
            transform      (t/translation-matrix 0 0 1)
            sphere         (s/make-sphere material transform)
            light          (make-light [0 0 -10 1] [1 1 1])
            scene          (e/make-scene [sphere] light)
            surface-normal [0 0 -1 0]
            surface-point  [0 0 0 1]
            eye-direction  [0 0.70711 -0.70711 0]
            prepared-hit   {:shape sphere
                            :surface-normal surface-normal
                            :surface-point surface-point
                            :eye-direction eye-direction}]
        (is (≈ [0.1 0.1 0.1] (ambient light prepared-hit)))
        (is (≈ [0.9 0.9 0.9] (diffuse light prepared-hit)))
        (is (≈ [0.0 0.0 0.0] (specular light prepared-hit)))
        (is (≈ [1.0 1.0 1.0] (lighting scene prepared-hit)))))
    (testing "lighting with opposite surface, light offset 45°"
      ;;
      ;;                     🔆       _____
      ;;                       ⟍   ⟋       ⟍
      ;;                         ⟍/          \
      ;;            👁        <----|          |
      ;;                           ⟍        ⟋
      ;;                              ‾‾‾‾‾
      ;;
      (let [material       a/default-material
            transform      (t/translation-matrix 0 0 1)
            sphere         (s/make-sphere material transform)
            light          (make-light [0 10 -10 1] [1 1 1])
            scene          (e/make-scene [sphere] light)
            surface-normal [0 0 -1 0]
            surface-point  [0 0 0 1]
            eye-direction  [0 0 -1 0]
            prepared-hit   {:shape sphere
                            :surface-normal surface-normal
                            :surface-point surface-point
                            :eye-direction eye-direction}]
        (is (≈ [0.1 0.1 0.1] (ambient light prepared-hit)))
        (is (≈ [0.6364 0.6364 0.6364] (diffuse light prepared-hit)))
        (is (≈ [0.0 0.0 0.0] (specular light prepared-hit)))
        (is (≈ [0.7364 0.7364 0.7364] (lighting scene prepared-hit)))))
    (testing "lighting with eye in the path of the reflection vector"
      ;;
      ;;                     🔆       _____
      ;;                       ⟍   ⟋       ⟍
      ;;                         ⟍/          \
      ;;                     <----|          |
      ;;                         ⟋ ⟍       ⟋
      ;;                       ⟋     ‾‾‾‾‾
      ;;                     👁
      ;;
      (let [material       a/default-material
            transform      (t/translation-matrix 0 0 1)
            sphere         (s/make-sphere material transform)
            light          (make-light [0 10 -10 1] [1 1 1])
            scene          (e/make-scene [sphere] light)
            surface-normal [0 0 -1 0]
            surface-point  [0 0 0 1]
            eye-direction  [0 (* -0.5 (Math/sqrt 2)) (* -0.5 (Math/sqrt 2)) 0]
            prepared-hit   {:shape sphere
                            :surface-normal surface-normal
                            :surface-point surface-point
                            :eye-direction eye-direction}]
        (is (≈ [0.1 0.1 0.1] (ambient light prepared-hit)))
        (is (≈ [0.6364 0.6364 0.6364] (diffuse light prepared-hit)))
        (is (≈ [0.9 0.9 0.9] (specular light prepared-hit)))
        (is (≈ [1.6364 1.6364 1.6364] (lighting scene prepared-hit)))))
    (testing "lighting with the light behind the surface"
      ;;
      ;;                             _____
      ;;                           ⟋       ⟍
      ;;                          /          \
      ;;                👁   <----|---> 🔆    |
      ;;                           ⟍       ⟋
      ;;                             ‾‾‾‾‾‾
      ;;
      (let [material       a/default-material
            transform      (t/translation-matrix 0 0 1)
            sphere         (s/make-sphere material transform)
            light          (make-light [0 0 10 1] [1 1 1])
            scene          (e/make-scene [sphere] light)
            surface-normal [0 0 -1 0]
            surface-point  [0 0 0 1]
            eye-direction  [0 0 -1 0]
            prepared-hit   {:shape sphere
                            :surface-normal surface-normal
                            :surface-point surface-point
                            :eye-direction eye-direction}]
        (is (≈ [0.1 0.1 0.1] (ambient light prepared-hit)))
        (is (≈ [0.0 0.0 0.0] (diffuse light prepared-hit)))
        (is (≈ [0.0 0.0 0.0] (specular light prepared-hit)))
        (is (≈ [0.1 0.1 0.1] (lighting scene prepared-hit))))))

(deftest testing-color-for
  (testing "the color when a ray misses"
    (let [material1  (a/make-material {:color [0.8 1.0 0.6]
                                       :ambient 0.1
                                       :diffuse 0.7
                                       :specular 0.2
                                       :shininess 200
                                       :pattern nil})
          sphere1    (s/make-sphere material1)
          transform2 (t/scaling-matrix 0.5 0.5 0.5)
          sphere2    (s/make-sphere a/default-material transform2)
          world      (e/add-objects (e/make-scene) [sphere1 sphere2])
          ray        (r/make-ray [0 0 -5 1] [0 1 0 0])]
      (is (≈ [0 0 0] (color-for world ray)))))
  (testing "the color when a ray hits"
    (let [material1  (a/make-material {:color [0.8 1.0 0.6]
                                       :ambient 0.1
                                       :diffuse 0.7
                                       :specular 0.2
                                       :shininess 200
                                       :pattern nil})
          sphere1    (s/make-sphere material1)
          transform2 (t/scaling-matrix 0.5 0.5 0.5)
          sphere2    (s/make-sphere a/default-material transform2)
          world      (e/add-objects (e/make-scene) [sphere1 sphere2])
          ray        (r/make-ray [0 0 -5 1] [0 0 1 0])]
      (is (≈ [0.38066 0.47583 0.2855] (color-for world ray)))))
  (testing "the color with an intersection behind the ray"
    (let [material1  (a/make-material {:color [0.8 1.0 0.6]
                                       :ambient 1.0
                                       :diffuse 0.7
                                       :specular 0.2
                                       :shininess 200
                                       :pattern nil})
          sphere1    (s/make-sphere material1)
          material2  (a/make-material {:color [1 1 1]
                                       :ambient 1.0
                                       :diffuse 0.9
                                       :specular 0.9
                                       :shininess 200
                                       :pattern nil})
          transform2 (t/scaling-matrix 0.5 0.5 0.5)
          sphere2    (s/make-sphere material2 transform2)
          world      (e/add-objects (e/make-scene) [sphere1 sphere2])
          ray        (r/make-ray [0 0 0.75 1] [0 0 -1 0])]
      (is (≈ [1.0 1.0 1.0] (color-for world ray))))))
