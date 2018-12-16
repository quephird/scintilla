(ns scintilla.lighting-test
  (:require [clojure.test :refer :all]
            [scintilla.color :as c]
            [scintilla.lighting :refer :all]
            [scintilla.materials :as a]
            [scintilla.matrix :as m]
            [scintilla.numeric :refer :all]
            [scintilla.patterns :as p]
            [scintilla.ray :as r]
            [scintilla.scene :as e]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]
            [scintilla.tuple :as u]))

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
          over-point     (u/plus surface-point (u/scalar-times surface-normal ε))
          eye-direction  [0 0 -1 0]
          prepared-hit   {:shape          sphere
                          :surface-normal surface-normal
                          :surface-point  surface-point
                          :over-point     over-point
                          :eye-direction  eye-direction}]
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
            over-point     (u/plus surface-point (u/scalar-times surface-normal ε))
            eye-direction  [0 0.70711 -0.70711 0]
            prepared-hit   {:shape          sphere
                            :surface-normal surface-normal
                            :surface-point  surface-point
                            :over-point     over-point
                            :eye-direction  eye-direction}]
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
            over-point     (u/plus surface-point (u/scalar-times surface-normal ε))
            eye-direction  [0 0 -1 0]
            prepared-hit   {:shape          sphere
                            :surface-normal surface-normal
                            :surface-point  surface-point
                            :over-point     over-point
                            :eye-direction  eye-direction}]
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
            over-point     (u/plus surface-point (u/scalar-times surface-normal ε))
            eye-direction  [0 (* -0.5 (Math/sqrt 2)) (* -0.5 (Math/sqrt 2)) 0]
            prepared-hit   {:shape          sphere
                            :surface-normal surface-normal
                            :surface-point  surface-point
                            :over-point     over-point
                            :eye-direction  eye-direction}]
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
            over-point     (u/plus surface-point (u/scalar-times surface-normal ε))
            eye-direction  [0 0 -1 0]
            prepared-hit   {:shape          sphere
                            :surface-normal surface-normal
                            :surface-point  surface-point
                            :over-point     over-point
                            :eye-direction  eye-direction}]
        (is (≈ [0.1 0.1 0.1] (ambient light prepared-hit)))
        (is (≈ [0.0 0.0 0.0] (diffuse light prepared-hit)))
        (is (≈ [0.0 0.0 0.0] (specular light prepared-hit)))
        (is (≈ [0.1 0.1 0.1] (lighting scene prepared-hit))))))

(deftest testing-reflected-lighting
  (testing "the reflected color for a non-reflective material"
    (let [material1     (a/make-material {:color [0.8 1.0 0.6]
                                          :ambient 0.1
                                          :diffuse 0.7
                                          :specular 0.2
                                          :shininess 200
                                          :pattern nil})
          sphere1       (s/make-sphere material1)

          material2     (a/make-material {:color [1 1 1]
                                          :ambient 1.0
                                          :diffuse 0.9
                                          :specular 0.9
                                          :shininess 200
                                          :pattern nil})
          transform2    (t/scaling-matrix 0.5 0.5 0.5)
          sphere2       (s/make-sphere material2 transform2)

          scene         (e/add-objects (e/make-scene) [sphere1 sphere2])
          ray           (r/make-ray [0 0 0 1] [0 0 1 0])
          intersections (r/find-all-intersections scene ray)
          hit           (r/find-hit intersections)
          prepared-hit  (r/make-prepared-hit hit ray intersections)]
      (is (≈ [0 0 0] (reflected-lighting scene prepared-hit max-reflections)))))
  (testing "the reflected color for a reflective material"
    (let [material1     (a/make-material {:color [0.8 1.0 0.6]
                                          :ambient 0.1
                                          :diffuse 0.7
                                          :specular 0.2
                                          :shininess 200
                                          :pattern nil})
          sphere1       (s/make-sphere material1)

          material2     (a/make-material {:color [1 1 1]
                                          :ambient 1.0
                                          :diffuse 0.9
                                          :specular 0.9
                                          :shininess 200
                                          :pattern nil})
          transform2    (t/scaling-matrix 0.5 0.5 0.5)
          sphere2       (s/make-sphere material2 transform2)

          material3     (a/make-material {:reflective 0.5})
          transform3    (t/translation-matrix 0 -1 0)
          plane         (s/make-plane material3 transform3)

          scene         (e/add-objects (e/make-scene) [sphere1 sphere2 plane])
          ray           (r/make-ray [0 0 -3 1] [0 -0.7071 0.7071 0])
          intersections (r/find-all-intersections scene ray)
          hit           (r/find-hit intersections)
          prepared-hit  (r/make-prepared-hit hit ray intersections)]
      (is (≈ [0.19032 0.2379 0.14274] (reflected-lighting scene prepared-hit max-reflections)))))
  (testing "the reflected color at the maximum recursive depth"
    (let [material1     (a/make-material {:color [0.8 1.0 0.6]
                                          :ambient 0.1
                                          :diffuse 0.7
                                          :specular 0.2
                                          :shininess 200
                                          :pattern nil})
          sphere1       (s/make-sphere material1)

          material2     (a/make-material {:color [1 1 1]
                                          :ambient 1.0
                                          :diffuse 0.9
                                          :specular 0.9
                                          :shininess 200
                                          :pattern nil})
          transform2    (t/scaling-matrix 0.5 0.5 0.5)
          sphere2       (s/make-sphere material2 transform2)

          material3     (a/make-material {:reflective 0.5})
          transform3    (t/translation-matrix 0 -1 0)
          plane         (s/make-plane material3 transform3)

          scene         (e/add-objects (e/make-scene) [sphere1 sphere2 plane])
          ray           (r/make-ray [0 0 -3 1] [0 -0.7071 0.7071 0])
          intersections (r/find-all-intersections scene ray)
          hit           (r/find-hit intersections)
          prepared-hit  (r/make-prepared-hit hit ray intersections)])))

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
      (is (≈ [0 0 0] (color-for world ray max-reflections)))))
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
      (is (≈ [0.38066 0.47583 0.2855] (color-for world ray max-reflections)))))
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
      (is (≈ [1.0 1.0 1.0] (color-for world ray max-reflections)))))
  (testing "with a reflective material"
    (let [material1     (a/make-material {:color [0.8 1.0 0.6]
                                          :ambient 0.1
                                          :diffuse 0.7
                                          :specular 0.2
                                          :shininess 200
                                          :pattern nil})
          sphere1       (s/make-sphere material1)

          material2     (a/make-material {:color [1 1 1]
                                          :ambient 1.0
                                          :diffuse 0.9
                                          :specular 0.9
                                          :shininess 200
                                          :pattern nil})
          transform2    (t/scaling-matrix 0.5 0.5 0.5)
          sphere2       (s/make-sphere material2 transform2)

          material3     (a/make-material {:reflective 0.5})
          transform3    (t/translation-matrix 0 -1 0)
          plane         (s/make-plane material3 transform3)

          scene         (e/add-objects (e/make-scene) [sphere1 sphere2 plane])
          ray           (r/make-ray [0 0 -3 1] [0 -0.7071 0.7071 0])
          intersections (r/find-all-intersections scene ray)
          hit           (r/find-hit intersections)
          prepared-hit  (r/make-prepared-hit hit ray intersections)]
      (is (≈ [0.87677 0.92436 0.82918] (color-for scene ray max-reflections)))))
  (testing "recursion stops with mutually reflective surfaces"
    (let [lower-material  (a/make-material {:reflective 1})
          lower-transform (t/translation-matrix 0 -1 0)
          lower-plane     (s/make-plane lower-material lower-transform)

          upper-material  (a/make-material {:reflective 1})
          ;; TODO: See why we need to flip the plane here when
          ;;       the author notes this is unnecessary here:
          ;;
          ;;       https://pragprog.com/titles/jbtracer/errata?utf8=%E2%9C%93&what_to_show=2422#s83690
          upper-transform (t/translation-matrix 0 1 0)
          upper-plane     (s/make-plane lower-material upper-transform)

          light           (make-light [0 0 0 1] [1 1 1])
          scene           (e/make-scene [upper-plane lower-plane] light)

          ray             (r/make-ray [0 0 0 1] [0 1 0 0])
          color           (color-for scene ray max-reflections)]
      ;; There is no need to test anything specific here other than
      ;; observing that the computation halts predictably.
      )))

(deftest testing-refracted-lighting
  (testing "the refracted color with an opaque surface"
    (let [material1     (a/make-material {:color [0.8 1.0 0.6]
                                          :ambient 0.1
                                          :diffuse 0.7
                                          :specular 0.2
                                          :shininess 200
                                          :pattern nil})
          sphere1       (s/make-sphere material1)
          transform2    (t/scaling-matrix 0.5 0.5 0.5)
          sphere2       (s/make-sphere a/default-material transform2)
          scene         (e/add-objects (e/make-scene) [sphere1 sphere2])
          ray           (r/make-ray [0 0 -5 1] [0 0 1 0])
          intersections (r/find-all-intersections scene ray)
          hit           (r/find-hit intersections)
          prepared-hit  (r/make-prepared-hit hit ray intersections)]
      (is (≈ [0 0 0] (refracted-lighting scene prepared-hit max-reflections)))))
  (testing "the refracted color at the maximum recursive depth"
    (let [material1     (a/make-material {:color [0.8 1.0 0.6]
                                          :ambient 0.1
                                          :diffuse 0.7
                                          :specular 0.2
                                          :shininess 200
                                          :transparency 1.0
                                          :refractive-index 1.5
                                          :pattern nil})
          sphere1       (s/make-sphere material1)
          transform2    (t/scaling-matrix 0.5 0.5 0.5)
          sphere2       (s/make-sphere a/default-material transform2)
          scene         (e/add-objects (e/make-scene) [sphere1 sphere2])
          ray           (r/make-ray [0 0 -5 1] [0 0 1 0])
          intersections (r/find-all-intersections scene ray)
          hit           (r/find-hit intersections)
          prepared-hit  (r/make-prepared-hit hit ray intersections)]
      (is (≈ [0 0 0] (refracted-lighting scene prepared-hit 0)))))
  (testing "the refracted color from total internal reflection"
    (let [material1     (a/make-material {:color [0.8 1.0 0.6]
                                          :ambient 0.1
                                          :diffuse 0.7
                                          :specular 0.2
                                          :shininess 200
                                          :transparency 1.0
                                          :refractive-index 1.5
                                          :pattern nil})
          sphere1       (s/make-sphere material1)
          transform2    (t/scaling-matrix 0.5 0.5 0.5)
          sphere2       (s/make-sphere a/default-material transform2)
          scene         (e/add-objects (e/make-scene) [sphere1 sphere2])
          ray           (r/make-ray [0 0 0.7071 1] [0 1 0 0])
          intersections (r/find-all-intersections scene ray)
          second-hit    (second intersections)
          prepared-hit  (r/make-prepared-hit second-hit ray intersections)]
      (is (≈ [0 0 0] (refracted-lighting scene prepared-hit max-reflections)))))
  (testing "the refracted color with a refracted ray"
    (let [material1     (a/make-material {:ambient 1.0
                                          :color   nil
                                          :pattern p/test-pattern})
          sphere1       (s/make-sphere material1)

          material2     (a/make-material {:refractive-index 1.5
                                          :transparency     1.0})
          transform2    (t/scaling-matrix 0.5 0.5 0.5)
          sphere2       (s/make-sphere material2 transform2)

          scene         (e/add-objects (e/make-scene) [sphere1 sphere2])
          ray           (r/make-ray [0 0 0.1 1] [0 1 0 0])
          intersections (r/find-all-intersections scene ray)
          third-hit     (nth intersections 2)
          prepared-hit  (r/make-prepared-hit third-hit ray intersections)]
      (is (≈ [0 0.99888 0.04725] (refracted-lighting scene prepared-hit max-reflections))))))
