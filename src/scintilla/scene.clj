(ns scintilla.scene
  (:require [scintilla.lighting :as l]
            [scintilla.matrix :as m]
            [scintilla.transformation :as t]
            [scintilla.tuple :as u]))

(defn make-scene
  ([]
    (make-scene []))
  ([objects]
    (make-scene objects l/default-light))
  ([objects light]
    {:objects objects
     :light light}))

(defn add-objects
  [scene objects]
  (update-in scene [:objects] concat objects))

(defn pixel->scene
  "Converts the (x,y) coordinates in the canvas system
   to (x,y,z) coordinates of the scene world system"
  [[pixel-x pixel-y :as pixel]
   [canvas-w canvas-h :as canvas-dimensions]
   [wall-x wall-y wall-z _ :as wall-point]
   [wall-w wall-h :as wall-dimensions]]
   (let [scene-x (- (* wall-w (/ pixel-x canvas-w)) (/ wall-w 2.0))
         scene-y (- (* wall-h (/ pixel-y canvas-h)) (/ wall-h 2.0))]
     [scene-x scene-y wall-z 1.0]))
