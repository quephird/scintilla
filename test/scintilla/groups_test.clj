(ns scintilla.groups-test
  (:require [clojure.test :refer :all]
            [scintilla.groups :refer :all]
            [scintilla.matrix :as m :refer [I₄]]
            [scintilla.numeric :refer :all]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

(deftest testing-make-group
  (testing "empty group"
    (let [empty-group (make-group [])]
      (is (= (:children empty-group) []))))
  (testing "group of objects with no specified transform"
    (let [sphere  (s/make-sphere)
          cone    (s/make-cone)
          cube    (s/make-cube)
          group   (make-group [sphere cone cube])]
      (is (= (:children group) [sphere cone cube]))))
  (testing "transformed group of objects"
    (let [sphere    (s/make-sphere)
          cone      (s/make-cone)
          cube      (s/make-cube)
          transform (t/translation-matrix 1 2 3)
          group     (make-group [sphere cone cube] transform)]
      (is (= [:sphere :cone :cube]
             (->> group
                  :children
                  (map :shape-type))))
      (is (every? #(≈ % [[1 0 0 1]
                         [0 1 0 2]
                         [0 0 1 3]
                         [0 0 0 1]])
                  (->> group
                       :children
                       (map :transform))))))
  (testing "a multiply nested set of groups and objects"
    (let [sphere       (s/make-sphere)
          inner-group  (make-group [sphere]
                                   (t/translation-matrix 1 0 0))
          cone         (s/make-cone)
          middle-group (make-group [cone inner-group]
                                   (t/translation-matrix 0 2 0))
          cube         (s/make-cube)
          outer-group  (make-group [cube middle-group]
                                   (t/translation-matrix 0 0 3))]
      ;; Transform for cube
      (is (≈ [[1 0 0 0]
              [0 1 0 0]
              [0 0 1 3]
              [0 0 0 1]]
             (->> outer-group :children first :transform)))
      ;; Transform for cone
      (is (≈ [[1 0 0 0]
              [0 1 0 2]
              [0 0 1 3]
              [0 0 0 1]]
             (->> outer-group :children second
                              :children first :transform)))
      ;; Transform for sphere
      (is (≈ [[1 0 0 1]
              [0 1 0 2]
              [0 0 1 3]
              [0 0 0 1]]
             (->> outer-group :children second
                              :children second
                              :children first :transform))))))
            
(deftest testing-bounding-box
  (testing "group with one untransformed cube"
    (let [cube           (s/make-cube)
          group          (make-group [cube])
          expected-value I₄]
      (is (≈ expected-value (get-in group [:bounding-box :transform])))))
  (testing "group with one double-napped cone"
    (let [cone           (s/make-cone {:minimum -1 :maximum 1})
          group          (make-group [cone])
          expected-value I₄]
      (is (≈ expected-value (get-in group [:bounding-box :transform])))))
  (testing "group with one cone"
    (let [cone           (s/make-cone {:minimum 0 :maximum 2})
          group          (make-group [cone])
          expected-value (t/translation-matrix 0 1 0)]
      (is (≈ expected-value (get-in group [:bounding-box :transform])))))
  (testing "group with one scaled cube"
    (let [transform      (t/scaling-matrix 1 2 3)
          cube           (s/make-cube {:transform transform})
          group          (make-group [cube])
          expected-value transform]
      (is (≈ expected-value (get-in group [:bounding-box :transform])))))
  (testing "group with one rotated cube"
    (let [transform      (t/rotation-y-matrix π⟋4)
          cube           (s/make-cube {:transform transform})
          group          (make-group [cube])
          expected-value (t/scaling-matrix 1.4142 1 1.4142)]
      (is (≈ expected-value (get-in group [:bounding-box :transform])))))
  (testing "group with two translated cubes"
    (let [bottom-left-front-transform (t/translation-matrix -1 -1 -1)
          bottom-left-front-cube      (s/make-cube {:transform bottom-left-front-transform})
          top-right-back-transform    (t/translation-matrix 1 1 1)
          top-right-back-cube         (s/make-cube {:transform top-right-back-transform})
          group                       (make-group [bottom-left-front-cube top-right-back-cube])
          expected-value              (t/scaling-matrix 2 2 2)]
      (is (≈ expected-value (get-in group [:bounding-box :transform])))))
  (testing "nested group"
    (let [bottom-left-front-transform (t/translation-matrix -2 -2 -2)
          bottom-left-front-cube      (s/make-cube {:transform bottom-left-front-transform})
          bottom-left-back-transform  (t/translation-matrix -2 -2 -2)
          bottom-left-back-cube       (s/make-cube {:transform bottom-left-back-transform})
          bottom-left-group           (make-group [bottom-left-front-cube bottom-left-back-cube])

          top-right-front-transform   (t/translation-matrix 2 2 -1)
          top-right-front-cube        (s/make-cube {:transform top-right-front-transform})
          top-right-back-transform    (t/translation-matrix 2 2 2)
          top-right-back-cube         (s/make-cube {:transform top-right-back-transform})
          top-right-group             (make-group [top-right-front-cube top-right-back-cube])

          main-group                  (make-group [bottom-left-group top-right-group])
          expected-value              (t/scaling-matrix 3 3 3)]
      (is (≈ expected-value (get-in main-group [:bounding-box :transform])))))
  (testing "nested and transformed group"
    (let [bottom-left-front-transform (t/translation-matrix -2 -2 -2)
          bottom-left-front-cube      (s/make-cube {:transform bottom-left-front-transform})
          bottom-left-back-transform  (t/translation-matrix -2 -2 2)
          bottom-left-back-cube       (s/make-cube {:transform bottom-left-back-transform})
          bottom-left-group           (make-group [bottom-left-front-cube bottom-left-back-cube])

          top-right-front-transform   (t/translation-matrix 2 2 -2)
          top-right-front-cube        (s/make-cube {:transform top-right-front-transform})
          top-right-back-transform    (t/translation-matrix 2 2 2)
          top-right-back-cube         (s/make-cube {:transform top-right-back-transform})
          top-right-group             (make-group [top-right-front-cube top-right-back-cube])

          main-transform              (t/rotation-y-matrix π⟋4)
          main-group                  (make-group [bottom-left-group top-right-group] main-transform)
          expected-value              (t/scaling-matrix 4.2426 3 4.2426)]
      (is (≈ expected-value (get-in main-group [:bounding-box :transform]))))))
