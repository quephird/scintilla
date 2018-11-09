(ns scintilla.lighting-test
  (:require [clojure.test :refer :all]
            [scintilla.materials :as a]
            [scintilla.lighting :refer :all]
            [scintilla.numeric :refer [≈]]))

(deftest testing-lighting
  (let [material a/default-material
        surface-position [0 0 0 1]]
    (testing "lighting with the eye between the light and the surface

                                |
                                |
                   🔆   👁  <----|
                                |
                                |
      "
      (let [eye-direction [0 0 -1 0]
            surface-normal [0 0 -1 0]
            light (make-light [0 0 -10 1] [1 1 1])
            expected-value [1.9 1.9 1.9]]
        (is (≈ expected-value (lighting material light surface-position eye-direction surface-normal)))))
    (testing "lighting with the eye between light and surface, eye offset 45°

                           👁
                             ⟍  |
                               ⟍|
                   🔆       <----|
                                |
                                |
      "
      (let [eye-direction [0 0.70711 -0.70711 0]
            surface-normal [0 0 -1 0]
            light (make-light [0 0 -10 1] [1 1 1])
            expected-value [1.0 1.0 1.0]]
        (is (≈ expected-value (lighting material light surface-position eye-direction surface-normal)))))
    (testing "lighting with opposite surface, light offset 45°

                           🔆
                             ⟍  |
                               ⟍|
                   👁       <----|
                                |
                                |
      "
      (let [eye-direction [0 0 -1 0]
            surface-normal [0 0 -1 0]
            light (make-light [0 10 -10 1] [1 1 1])
            expected-value [0.7364 0.7364 0.7364]]
        (is (≈ expected-value (lighting material light surface-position eye-direction surface-normal)))))
    (testing "lighting with eye in the path of the reflection vector

                           🔆
                             ⟍  |
                               ⟍|
                            <----|
                               ⟋|
                             ⟋  |
                           👁
      "
      (let [eye-direction [0 -0.70711 -0.70711 0]
            surface-normal [0 0 -1 0]
            light (make-light [0 10 -10 1] [1 1 1])
            expected-value [1.63722 1.63722 1.63722]]
        (is (≈ expected-value (lighting material light surface-position eye-direction surface-normal)))))
        ))
