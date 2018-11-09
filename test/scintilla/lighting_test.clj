(ns scintilla.lighting-test
  (:require [clojure.test :refer :all]
            [scintilla.materials :as a]
            [scintilla.lighting :refer :all]
            [scintilla.numeric :refer [≈]]))

(deftest testing-lighting
  (let [material a/default-material
        surface-position [0 0 0 1]]
    (testing "lighting with the eye between the light and the surface"
      (let [eye-direction [0 0 -1 0]
            surface-normal [0 0 -1 0]
            light (make-light [0 0 -10 1] [1 1 1])
            expected-value [1.9 1.9 1.9]]
        (is (≈ expected-value (lighting material light surface-position eye-direction surface-normal)))))))
