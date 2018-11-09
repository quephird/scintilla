(ns scintilla.lighting-test
  (:require [clojure.test :refer :all]
            [scintilla.materials :as a]
            [scintilla.lighting :refer :all]
            [scintilla.numeric :refer [≈]]))

(deftest testing-lighting
  (let [material a/default-material
        surface-normal [0 0 -1 0]
        surface-position [0 0 0 1]]
    (testing "lighting with the eye between the light and the surface

                                |
                                |
                  🔆   👁  <----|
                                |
                                |
      "
      (let [eye-direction [0 0 -1 0]
            light (make-light [0 0 -10 1] [1 1 1])]
        (is (≈ [0.1 0.1 0.1] (ambient material light surface-position)))
        (is (≈ [0.9 0.9 0.9] (diffuse material light surface-position surface-normal)))
        (is (≈ [0.9 0.9 0.9] (specular material light surface-position eye-direction surface-normal)))
        (is (≈ [1.9 1.9 1.9] (lighting material light surface-position eye-direction surface-normal)))))
    (testing "lighting with the eye between light and surface, eye offset 45°

                           👁
                             ⟍  |
                               ⟍|
                  🔆       <----|
                                |
                                |
      "
      (let [eye-direction [0 0.70711 -0.70711 0]
            light (make-light [0 0 -10 1] [1 1 1])]
        (is (≈ [0.1 0.1 0.1] (ambient material light surface-position)))
        (is (≈ [0.9 0.9 0.9] (diffuse material light surface-position surface-normal)))
        (is (≈ [0.0 0.0 0.0] (specular material light surface-position eye-direction surface-normal)))
        (is (≈ [1.0 1.0 1.0] (lighting material light surface-position eye-direction surface-normal)))))
    (testing "lighting with opposite surface, light offset 45°

                           🔆
                             ⟍  |
                               ⟍|
                  👁       <----|
                                |
                                |
      "
      (let [eye-direction [0 0 -1 0]
            light (make-light [0 10 -10 1] [1 1 1])]
        (is (≈ [0.1 0.1 0.1] (ambient material light surface-position)))
        (is (≈ [0.6364 0.6364 0.6364] (diffuse material light surface-position surface-normal)))
        (is (≈ [0.0 0.0 0.0] (specular material light surface-position eye-direction surface-normal)))
        (is (≈ [0.7364 0.7364 0.7364] (lighting material light surface-position eye-direction surface-normal)))))
    (testing "lighting with eye in the path of the reflection vector

                           🔆
                             ⟍  |
                               ⟍|
                           <----|
                               ⟋|
                             ⟋  |
                           👁
      "
      (let [eye-direction [0 (* -0.5 (Math/sqrt 2)) (* -0.5 (Math/sqrt 2)) 0]
            light (make-light [0 10 -10 1] [1 1 1])]
        (is (≈ [0.1 0.1 0.1] (ambient material light surface-position)))
        (is (≈ [0.6364 0.6364 0.6364] (diffuse material light surface-position surface-normal)))
        (is (≈ [0.9 0.9 0.9] (specular material light surface-position eye-direction surface-normal)))
        (is (≈ [1.6364 1.6364 1.6364] (lighting material light surface-position eye-direction surface-normal)))))
    (testing "lighting with the light behind the surface

                                |
                                |
                      👁   <----|----> 🔆
                                |
                                |
      "
      (let [eye-direction [0 0 -1 0]
            light (make-light [0 0 10 1] [1 1 1])]
        (is (≈ [0.1 0.1 0.1] (ambient material light surface-position)))
        (is (≈ [0.0 0.0 0.0] (diffuse material light surface-position surface-normal)))
        (is (≈ [0.0 0.0 0.0] (specular material light surface-position eye-direction surface-normal)))
        (is (≈ [0.1 0.1 0.1] (lighting material light surface-position eye-direction surface-normal)))))
        ))
