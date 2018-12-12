(ns scintilla.rendering
  (:require [scintilla.file :as f]
            [scintilla.lighting :as l]
            [scintilla.ray :as r]))

(defn render
  "Renders the scene using the camera and returns the image data in a canvas data structure."
  [{:keys [pixel-width pixel-height] :as camera} scene]
  (into []
    (for [y (range pixel-height)]
      (into []
        (for [x (range pixel-width)]
          (l/color-for scene (r/ray-for camera x y) l/max-reflections))))))

(defn render-to-file
  "Renders the scene using the camera and saves the image to a file."
  [camera scene filename]
  (-> camera
      (render scene)
      (f/save-as-ppm filename)))
