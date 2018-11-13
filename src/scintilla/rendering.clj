(ns scintilla.rendering
  (:require [scintilla.file :as f]
            [scintilla.lighting :as l]
            [scintilla.materials :as a]
            [scintilla.ray :as r]
            [scintilla.scene :as e]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]
            [scintilla.tuple :refer :all]))

;; TODO: Think of some tests for this namespace!!!
;; TODO: Need to package wall and camera metrics into perspective box
(defn pixel-color
  "Computes the color of the pixel at the x,y coordinates
   for the given scene and canvas"
  [[x y]
   {:keys [light objects] :as scene}
   wall-center
   wall-dimensions
   canvas-dimensions
   camera-point]
  (let [;; TODO: Handle more than one object
        sphere (first objects)
        ;; Convert to scene world coordinates
        wall-point (e/pixel->scene [x y] canvas-dimensions wall-center wall-dimensions)
        ;; Compute the ray between the camera and the pixel
        direction (normalize (- wall-point camera-point))
        ;; Construct new ray from camera to wall
        ray (r/make-ray camera-point direction)
        ;; See if the ray intersects anything
        intersections (r/find-intersections sphere ray)
        ;; Find the closest hit, if any
        hit (r/find-hit intersections)]
    ;; If there's a hit...
    (if hit
      ;; ... then set the color of the pixel to that computed for the hit object...
      (l/lighting light (r/make-prepared-hit hit ray))
      ;; ... else set the pixel to black
      [0.0 0.0 0.0])))

(defn render
  "Produces new canvas, with specified dimensions, with object scene rendered to it"
  [scene [canvas-w canvas-h :as canvas-dimensions]]
  (let [wall-dimensions [7.0 7.0]
        camera-point [0.0 0.0 -5.0 1]
        wall-center [0.0 0.0 10.0 1]]
    ;; For each pixel in the canvas...
    (into []
      (for [x (range canvas-w)]
        (into []
          (for [y (range canvas-h)]
            (pixel-color [x y] scene wall-center wall-dimensions canvas-dimensions camera-point)))))))

(defn render-to-file
  [scene canvas-dimensions filename]
  (-> scene
      (render canvas-dimensions)
      (f/save-as-ppm filename)))
