(ns scintilla.rendering
  (:require [scintilla.camera :as c]
            [scintilla.file :as f]
            [scintilla.lighting :as l]))

(defn render
  "Renders the scene using the camera and returns the image data in a canvas data structure."
  [{:keys [pixel-width pixel-height] :as camera} scene]
  (into []
    (partition pixel-width
      (pmap (fn [[y x]]
              (l/color-for scene (c/ray-for camera x y) l/max-reflections))
            (for [y (range pixel-height) x (range pixel-width)]
              [y x])))))

(defn render-to-file
  "Renders the scene using the camera and saves the image to a file."
  [camera scene filename]
  (-> camera
      (render scene)
      (f/save-as-ppm filename)))
