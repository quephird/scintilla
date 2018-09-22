(ns scintilla.io-test
  (:require [clojure.test :refer :all]
            [scintilla.io :refer :all]
            [scintilla.canvas :as c]))

(deftest testing-ppm-header
  (testing "header includes dimensions of canvas"
    (let [canvas (c/make-canvas 640 480)
          header (ppm-header canvas)]
      (is (= "P3\n640 480\n255" header)))))
