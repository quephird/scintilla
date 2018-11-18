(ns scintilla.transformation
  (:require [scintilla.matrix :as m]
            [scintilla.tuple :as u]))

; Just some cute shortcuts to make things look more mathy.
(defn sin [θ] (Math/sin θ))
(defn -sin [θ] (- (Math/sin θ)))
(defn cos [θ] (Math/cos θ))
(defn -cos [θ] (- (Math/cos θ)))

; These next six functions return 4 ⨯ 4 matrices
; represented by their respective function names.
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

; These next six functions return 1 ⨯ 4 tuples representing
; points transformed by their respective strategies.
(defn translate
  [p x y z]
  (let [T (translation-matrix x y z)]
    (m/tuple-times T p)))

(defn scale
  [p x y z]
  (let [S (scaling-matrix x y z)]
    (m/tuple-times S p)))

(defn rotate-x
  [p θ]
  (let [Rx (rotation-x-matrix θ)]
    (m/tuple-times Rx p)))

(defn rotate-y
  [p θ]
  (let [Ry (rotation-y-matrix θ)]
    (m/tuple-times Ry p)))

(defn rotate-z
  [p θ]
  (let [Rz (rotation-z-matrix θ)]
    (m/tuple-times Rz p)))

(defn shear
  [p [xy xz yx yz zx zy]]
  (let [S (shearing-matrix xy xz yx yz zx zy)]
    (m/tuple-times S p)))

(defn view-transform-matrix-for
  [from-point to-point up-vector]
  (let [forward-vector (u/normalize (u/subtract to-point from-point))
        left-vector    (u/cross-product forward-vector (u/normalize up-vector))
        true-up-vector (u/cross-product left-vector forward-vector)
        orientation-matrix [left-vector
                            true-up-vector
                            (u/subtract forward-vector)
                            [0 0 0 1]]
        translation-matrix (->> from-point
                                u/subtract
                                (take 3)
                                (apply translation-matrix))]
    (m/matrix-times orientation-matrix translation-matrix)))
