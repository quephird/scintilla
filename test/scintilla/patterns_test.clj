(ns scintilla.patterns-test
  (:require [clojure.test :refer :all]
            [scintilla.patterns :refer :all]
            [scintilla.numeric :refer [≈]]))

(deftest testing-color-for-stripe-pattern
  (let [white   [1 1 1]
        black   [0 0 0]
        stripes (make-stripe-pattern white black)]
    (testing "A stripe pattern is constant in y"
      (let [point-1       [0 0 0 1]
            point-2       [0 1 0 1]
            point-3       [0 2 0 1]
            prepared-hits (map (fn [p]
                                 {:shape {:material {:pattern stripes}}
                                  :surface-point p}) [point-1 point-2 point-3])]
        (is (≈ [white white white] (map #(color-for %) prepared-hits)))))
    (testing "A stripe pattern is constant in z"
      (let [point-1       [0 0 0 1]
            point-2       [0 0 1 1]
            point-3       [0 0 2 1]
            prepared-hits (map (fn [p]
                                 {:shape {:material {:pattern stripes}}
                                  :surface-point p}) [point-1 point-2 point-3])]
        (is (≈ [white white white] (map #(color-for %) prepared-hits)))))
    (testing "A stripe pattern alternates along the x axis, one unit at a time"
      (let [point-1       [0 0 0 1]
            point-2       [0.9 0 0 1]
            point-3       [1.0 0 0 1]
            point-4       [-0.1 0 0 1]
            point-5       [-1.0 0 0 1]
            point-6       [-1.1 0 0 1]
            all-points    [point-1 point-2 point-3 point-4 point-5 point-6]
            prepared-hits (map (fn [p]
                                 {:shape {:material {:pattern stripes}}
                                  :surface-point p}) all-points)]
        (is (≈ [white white black black black white]
               (map #(color-for %) prepared-hits)))))))
    
