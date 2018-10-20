(ns scintilla.transformation
  (:require [scintilla.matrix :refer :all]))

(defn translation-matrix
  [x y z]
  [[1 0 0 x]
   [0 1 0 y]
   [0 0 1 z]
   [0 0 0 1]])

(defn scaling-matrix
  [x y z]
  [[x 0 0 0]
   [0 y 0 0]
   [0 0 z 0]
   [0 0 0 1]])

(defn rotation-matrix
  [x y z]
  [[1 0 0 x]
   [0 1 0 y]
   [0 0 1 z]
   [0 0 0 1]])

(defn translate
  [p x y z]
  (let [T (translation-matrix x y z)]
    (tuple-times T p)))

(defn scale
  [p x y z]
  (let [S (scaling-matrix x y z)]
    (tuple-times S p)))
