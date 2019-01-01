(ns scintilla.groups-test
  (:require [clojure.test :refer :all]
            [scintilla.groups :refer :all]
            [scintilla.ray :as r]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

(deftest testing-all-intersections-for
  (testing "group with no children"
    (let [group (make-group)
          ray   (r/make-ray [0 0 0 1] [0 0 1 0])]
      (is (= [] (all-intersections-for group ray)))))
  (testing "a non-empty group"
    (let [sphere-1        (s/make-sphere)

          transform-2     (t/translation-matrix 0 0 -3)
          sphere-2        (s/make-sphere {:transform transform-2})

          transform-3     (t/translation-matrix 5 0 0)
          sphere-3        (s/make-sphere {:transform transform-3})

          spheres         (make-group [sphere-1 sphere-2 sphere-3])

          ray             (r/make-ray [0 0 -5 1] [0 0 1 0])
          intersections   (all-intersections-for spheres ray)
          expected-values [sphere-2 sphere-2 sphere-1 sphere-1]]
      (is (= expected-values (map :shape (all-intersections-for spheres ray))))))
  (testing "a transformed group"
    (let [transform-o     (t/translation-matrix 5 0 0)
          sphere          (s/make-sphere {:transform transform-o})

          transform-g     (t/scaling-matrix 2 2 2)
          group           (make-group [sphere] transform-g)

          ray             (r/make-ray [10 0 -10 1] [0 0 1 0])
          intersections   (all-intersections-for group ray)]
      (is (= 2 (count intersections))))))

