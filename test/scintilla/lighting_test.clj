(ns scintilla.lighting-test
  (:require [clojure.test :refer :all]
            [scintilla.materials :as a]
            [scintilla.lighting :refer :all]
            [scintilla.numeric :refer [≈]]))

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
