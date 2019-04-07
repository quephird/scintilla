(ns scintilla.obj-test
  (:require [clojure.test :refer :all]
            [scintilla.numeric :refer [≈]]
            [scintilla.obj :refer :all]))

(deftest testing-load
  (testing "file with gibberish"
    (is (thrown-with-msg? RuntimeException
                          #"Cannot parse line"
                          (load "gibberish.obj"))))
  (testing "file with just vertices"
    (let [expected-value [[-1 1 0]
                          [-1.0000 0.5000 0.0000]
                          [1 0 0]
                          [1 1 0]]]
      (is (≈ expected-value (:vertices (load "just_vertices.obj"))))))
)
