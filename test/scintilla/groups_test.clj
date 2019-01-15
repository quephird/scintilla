(ns scintilla.groups-test
  (:require [clojure.test :refer :all]
            [scintilla.groups :refer :all]
            [scintilla.numeric :refer :all]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

(deftest testing-make-group
  (testing "empty group"
    (is (= {:object-type :group
            :children    []} (make-group []))))
  (testing "group of objects with no specified transform"
    (let [sphere  (s/make-sphere)
          cone    (s/make-cone)
          cube    (s/make-cube)
          group   (make-group [sphere cone cube])]
      (is (= {:object-type :group
              :children    [sphere cone cube]} group))))
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
            
