(ns scintilla.mtl-test
  (:require [clojure.test :refer :all]
            [scintilla.numeric :refer [≈]]
            [scintilla.mtl :refer :all]))

(deftest testing-parse-file
  (testing "file with multiple materials"
    (let [parsed-results    (parse-file "materials.mtl")
          expected-materials  {:FirstMaterial {:ambient [1.0 1.0 1.0]
                                               :diffuse [0.7 0.6 0.5]
                                               :shininess 20.0
                                               :specular [0.2 0.2 0.25]}
                               :SecondMaterial {:ambient [0.5 0.0 1.0]
                                                :diffuse [0.2 0.2 0.2]
                                                :shininess 100.0
                                                :specular [0.0 0.1 0.15]}}]
      (testing "current-material is last material"
        (is (= :SecondMaterial (:current-material parsed-results))))
      (testing "all attributes are successfully parsed"
        (is (= expected-materials (:materials parsed-results))))
      )
    )
)
