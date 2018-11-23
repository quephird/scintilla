(ns scintilla.rendering
  (:require [scintilla.camera :as c]
            [scintilla.file :as f]
            [scintilla.ray :as r]))

(defn render
  "Renders the scene using the camera and returns the image data in a canvas data structure."
  [{:keys [pixel-width pixel-height] :as camera} scene]
  (into []
    (for [y (range pixel-height)]
      (into []
        (for [x (range pixel-width)]
          (r/color-for scene (c/ray-for camera x y)))))))

(defn render-to-file
  "Renders the scene using the camera and saves the image to a file."
  [camera scene filename]
  (-> camera
      (render scene)
      (f/save-as-ppm filename)))
