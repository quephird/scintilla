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
                       (map :transform)))))))
            
