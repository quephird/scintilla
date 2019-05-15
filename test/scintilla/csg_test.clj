(ns scintilla.csg-test
    (:require [clojure.test :refer :all]
              [scintilla.csg :refer :all]))

(deftest testing-intersection-allowed?
  (testing "all possibilities for unions"
    (let [all-inputs [[true true true]
                      [true true false]
                      [true false true]
                      [true false false]
                      [false true true]
                      [false true false]
                      [false false true]
                      [false false false]]
          all-expected-results [false
                                true
                                false
                                true
                                false
                                false
                                true
                                true]]
      (is (= all-expected-results
             (map #(apply intersection-allowed? :union %) all-inputs)))))
  ) 
