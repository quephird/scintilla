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
      (is (= body expected-value))))

  (testing "body has no lines longer than 70 characters"
    (let [canvas         (reduce (fn [acc [x y]]
                                   (c/write-pixel acc x y [1 0.8 0.6]))
                                 (c/make-canvas 10 2)
                                 (for [x (range 10) y (range 2)] [x y]))
          body           (ppm-body canvas)
          lines          ["255 204 153 255 204 153 255 204 153 255 204 153 255 204 153 255 204"
                          "153 255 204 153 255 204 153 255 204 153 255 204 153"
                          "255 204 153 255 204 153 255 204 153 255 204 153 255 204 153 255 204"
                          "153 255 204 153 255 204 153 255 204 153 255 204 153"]
          expected-value (clojure.string/join "\n" lines)]
      (is (= body expected-value))))
      )
