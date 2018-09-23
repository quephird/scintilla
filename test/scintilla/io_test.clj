(ns scintilla.io-test
  (:require [clojure.test :refer :all]
            [scintilla.io :refer :all]
            [scintilla.canvas :as c]))

(deftest testing-ppm-header
  (testing "header includes dimensions of canvas"
    (let [canvas (c/make-canvas 640 480)
          header (ppm-header canvas)]
      (is (= "P3\n640 480\n255" header)))))

(deftest testing-ppm-body
  (testing "body is written out properly"
    (let [canvas         (-> (c/make-canvas 5 3)
                             (c/write-pixel 0 0 [1.5 0 0])
                             (c/write-pixel 2 1 [0 0.5 0])
                             (c/write-pixel 4 2 [0 0 1]))
          body           (ppm-body canvas)
          expected-value (clojure.string/join "\n" ["255 0 0 0 0 0 0 0 0 0 0 0 0 0 0"
                                                    "0 0 0 0 0 0 0 128 0 0 0 0 0 0 0"
                                                    "0 0 0 0 0 0 0 0 0 0 0 0 0 0 255"])]
      (is (= body expected-value)))))
