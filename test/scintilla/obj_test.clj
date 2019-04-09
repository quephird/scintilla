(ns scintilla.obj-test
  (:require [clojure.test :refer :all]
            [scintilla.numeric :refer [≈]]
            [scintilla.obj :refer :all]
            [scintilla.shapes :as s]))

(deftest testing-parse-file
  (testing "file with gibberish"
    (is (thrown-with-msg? RuntimeException
                          #"Cannot parse line"
                          (parse-file "gibberish.obj"))))
  (testing "file with comments"
    (let [parsed-results (parse-file "just_comments.obj")]
      (is (zero? (count (:vertices parsed-results))))
      (is (empty? (get-in parsed-results [:groups :default])))))
  (testing "file with just vertices"
    (let [parsed-results (parse-file "just_vertices.obj")
          expected-value [[-1 1 0]
                          [-1.0000 0.5000 0.0000]
                          [1 0 0]
                          [1 1 0]]]
      (is (≈ expected-value (:vertices parsed-results)))
      (is (empty? (get-in parsed-results [:groups :default])))))
  (testing "file with triangle faces"
    (let [parsed-results    (parse-file "triangle_faces.obj")
          expected-vertices [[-1 1 0]
                             [-1 0 0]
                             [1 0 0]
                             [1 1 0]]
          expected-groups {:default [[1 2 3]
                                     [1 3 4]]}]
      (is (≈ expected-vertices (:vertices parsed-results)))
      (is (= expected-groups (:groups parsed-results)))))
  (testing "file with polygon"
    (let [parsed-results    (parse-file "polygon.obj")
          expected-vertices [[-1 1 0]
                             [-1 0 0]
                             [1 0 0]
                             [1 1 0]
                             [0 2 0]]
          expected-groups   {:default [[1 2 3]
                                       [1 3 4]
                                       [1 4 5]]}]
      (is (≈ expected-vertices (:vertices parsed-results)))
      (is (= expected-groups (:groups parsed-results)))))
)
