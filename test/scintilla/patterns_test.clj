(ns scintilla.patterns-test
  (:require [clojure.test :refer :all]
            [scintilla.materials :as a]
            [scintilla.matrix :refer [I₄]]
            [scintilla.patterns :refer :all]
            [scintilla.numeric :refer [≈]]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

(def white [1 1 1])
(def black [0 0 0])

(deftest testing-color-for-stripe-pattern
  (let [stripes (make-stripe-pattern white black)]
    (testing "A stripe pattern is constant in y"
      (let [point-1       [0 0 0 1]
            point-2       [0 1 0 1]
            point-3       [0 2 0 1]
            prepared-hits (map (fn [p]
                                 {:shape {:material {:pattern stripes}
                                          :transform I₄}
                                  :surface-point p}) [point-1 point-2 point-3])]
        (is (≈ [white white white] (map #(color-for %) prepared-hits)))))
    (testing "A stripe pattern is constant in z"
      (let [point-1       [0 0 0 1]
            point-2       [0 0 1 1]
            point-3       [0 0 2 1]
            prepared-hits (map (fn [p]
                                 {:shape {:material {:pattern stripes}
                                          :transform I₄}
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
                                 {:shape {:material {:pattern stripes}
                                          :transform I₄}
                                  :surface-point p}) all-points)]
        (is (≈ [white white black black black white]
               (map #(color-for %) prepared-hits)))))))

(deftest testing-color-for-stripe-pattern-with-transformations
  (testing "stripes with an object transformation"
    (let [transform     (t/scaling-matrix 2 2 2)
          stripes       (make-stripe-pattern white black)
          material      (a/set-pattern a/default-material stripes)
          sphere        (s/make-sphere {:material material
                                        :transform transform})
          prepared-hit  {:shape sphere :surface-point [1.5 0 0]}]
      (is (≈ white (color-for prepared-hit)))))
  (testing "stripes with an pattern transformation"
    (let [transform     (t/scaling-matrix 2 2 2)
          stripes       (make-stripe-pattern white black transform)
          material      (a/set-pattern a/default-material stripes)
          sphere        (s/make-sphere {:material material})
          prepared-hit  {:shape sphere :surface-point [1.5 0 0]}]
      (is (≈ white (color-for prepared-hit)))))
  (testing "stripes with obejct and pattern transformations"
    (let [pat-xform     (t/translation-matrix 0.5 0 0)
          stripes       (make-stripe-pattern white black pat-xform)
          material      (a/set-pattern a/default-material stripes)
          obj-xform     (t/scaling-matrix 2 2 2)
          sphere        (s/make-sphere {:material material
                                        :transform obj-xform})
          prepared-hit  {:shape sphere :surface-point [1.5 0 0]}]
      (is (≈ white (color-for prepared-hit))))))

(deftest testing-color-for-ring-pattern
  (testing "ring should extend in both x and z"
    (let [rings         (make-ring-pattern white black)
          point-1       [0 0 0 1]
          point-2       [1 0 0 1]
          point-3       [0 0 1 1]
          ;; the next point is slightly more than √2/2 away from origin
          point-4       [0.708 0 0.708]
          prepared-hits (map (fn [p]
                               {:shape {:material {:pattern rings}
                                        :transform I₄}
                                :surface-point p}) [point-1 point-2 point-3 point-4])
          expected-values [white black black black]]
      (is (≈ expected-values (map #(color-for %) prepared-hits))))))

(deftest testing-color-for-gradient-pattern
  (testing "gradient linearly interpolates colors"
    (let [gradient      (make-gradient-pattern white black)
          point-1       [0.0  0 0 1]
          point-2       [0.25 0 0 1]
          point-3       [0.5  0 0 1]
          point-4       [0.75 0 0 1]
          prepared-hits (map (fn [p]
                               {:shape {:material {:pattern gradient}
                                        :transform I₄}
                                :surface-point p}) [point-1 point-2 point-3 point-4])
          expected-values [[1 1 1] [0.75 0.75 0.75] [0.5 0.5 0.5] [0.25 0.25 0.25]]]
      (is (≈ expected-values (map #(color-for %) prepared-hits))))))

(deftest testing-color-for-checker-pattern
  (let [checkers   (make-checker-pattern white black)]
    (testing "checkers should repeat in x"
      (let [points          [[0 0 0] [0.99 0 0] [1.01 0 0]]
            expected-values  [white     white      black]
            prepared-hits (map (fn [p]
                                 {:shape {:material {:pattern checkers}
                                          :transform I₄}
                                  :over-point p}) points)]
        (is (≈ expected-values (map #(color-for %) prepared-hits)))))
    (testing "checkers should repeat in y"
      (let [points          [[0 0 0] [0 0.99 0] [0 1.01 0]]
            expected-values  [white     white      black]
            prepared-hits (map (fn [p]
                                 {:shape {:material {:pattern checkers}
                                          :transform I₄}
                                  :over-point p}) points)]
        (is (≈ expected-values (map #(color-for %) prepared-hits)))))
    (testing "checkers should repeat in z"
      (let [points          [[0 0 0] [0 0 0.99] [0 0 1.01]]
            expected-values  [white     white      black]
            prepared-hits (map (fn [p]
                               {:shape {:material {:pattern checkers}
                                        :transform I₄}
                                :over-point p}) points)]
        (is (≈ expected-values (map #(color-for %) prepared-hits)))))))
