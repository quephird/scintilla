(ns scintilla.scene
  (:require [scintilla.canvas :as c]
            [scintilla.matrix :as m]
            [scintilla.ray :as r]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]
            [scintilla.tuple :refer :all]))

; perspective rectangle
;   * width in scene world
;   * height in scene world
;   * depth in scene world

;; TODO: Consider not using protocols for vector types;
;; they make this code too verbose.
(defn pixel->scene
  "Converts the (x,y) coordinates in the canvas system
   to (x,y,z) coordinates of the scene world system"
  [[pixel-x pixel-y :as pixel]
   [canvas-w canvas-h :as canvas-dimensions]
   [wall-x wall-y wall-z _ :as wall-point]
   [wall-w wall-h :as wall-dimensions]]
   (let [scene-x (clojure.core/- (clojure.core/* wall-w (clojure.core// pixel-x canvas-w)) (clojure.core// wall-w 2.0))
         scene-y (clojure.core/- (clojure.core/* wall-h (clojure.core// pixel-y canvas-h)) (clojure.core// wall-h 2.0))]
     [scene-x scene-y wall-z 1.0]))

(defn render
  "Produces new canvas, with specified dimensions, with object scene rendered to it"
  [scene [canvas-w canvas-h :as canvas-dimensions]]
  ;; NOTA BENE: At some point all of these object specifications
  ;; ought to be passed into this function.
  (let [wall-dimensions [7.0 7.0]
        camera-point [0.0 0.0 -5.0 1]
        wall-center [0.0 0.0 10.0 1]
        transform (m/matrix-times (t/shearing-matrix 1.0 0.0 0.0 0.0 0.0 0.0)
                                  (t/scaling-matrix 0.5 1.0 1.0))
        sphere  (s/make-sphere [0 1 0] transform)]
    ;; For each pixel in the canvas...
    (into []
      (for [x (range canvas-w)]
        (into []
          (for [y (range canvas-h)]
            (let [;; Convert to scene world coordinates
                  wall-point (pixel->scene [x y] canvas-dimensions wall-center wall-dimensions)
                  ;; Compute the ray between the camera and the pixel
                  direction (- wall-point camera-point)
                  ;; Construct new ray from camera to wall
                  ray (r/make-ray camera-point direction)
                  ;; See if the ray intersects anything
                  intersections (r/find-intersections sphere ray)
                  ;; Find the closest hit, if any
                  hit (r/find-hit intersections)]
              ;; If there's a hit...
              (if hit
                ;; ... then set the color of the pixel to that of the hit object...
                (get-in hit [:shape :color])
                ;; ... else set the pixel to black
                [0.0 0.0 0.0]))))))))
