(ns scintilla.transformation
  (:require [scintilla.matrix :refer :all]))

(defn sin [θ] (Math/sin θ))
(defn -sin [θ] (- (Math/sin θ)))
(defn cos [θ] (Math/cos θ))
(defn -cos [θ] (- (Math/cos θ)))

(def π 3.1415926536)
(def π⟋2 (/ π 2.0))
(def π⟋3 (/ π 3.0))
(def π⟋4 (/ π 4.0))
(def π⟋6 (/ π 6.0))

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

(defn rotation-x-matrix
  [θ]
  [[1.0     0.0      0.0  0.0]
   [0.0 (cos θ) (-sin θ)  0.0]
   [0.0 (sin θ)  (cos θ)  0.0]
   [0.0     0.0      0.0  1.0]])

(defn rotation-y-matrix
  [θ]
  [[ (cos θ) 0.0  (sin θ)  0.0]
   [    0.0  1.0      0.0  0.0]
   [(-sin θ) 0.0  (cos θ)  0.0]
   [    0.0  0.0      0.0  1.0]])

(defn rotation-z-matrix
  [θ]
  [[(cos θ) (-sin θ)  0.0  0.0]
   [(sin θ)  (cos θ)  0.0  0.0]
   [    0.0      0.0  1.0  0.0]
   [    0.0      0.0  0.0  1.0]])

(defn shearing-matrix
  [xy xz yx yz zx zy]
  [[ 1 xy xz  0]
   [yx  1 yz  0]
   [zx zy  1  0]
   [ 0  0  0  1]])

(defn translate
  [p x y z]
  (let [T (translation-matrix x y z)]
    (tuple-times T p)))

(defn scale
  [p x y z]
  (let [S (scaling-matrix x y z)]
    (tuple-times S p)))

(defn rotate-x
  [p θ]
  (let [Rx (rotation-x-matrix θ)]
    (tuple-times Rx p)))

(defn rotate-y
  [p θ]
  (let [Ry (rotation-y-matrix θ)]
    (tuple-times Ry p)))

(defn rotate-z
  [p θ]
  (let [Rz (rotation-z-matrix θ)]
    (tuple-times Rz p)))

(defn shear
  [p [xy xz yx yz zx zy]]
  (let [S (shearing-matrix xy xz yx yz zx zy)]
    (tuple-times S p)))
