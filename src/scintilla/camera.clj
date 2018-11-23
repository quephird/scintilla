(ns scintilla.camera
  (:require [scintilla.matrix :refer [I₄] :as m]
            [scintilla.tuple :as u]))

(defn make-camera
  "Constructs a camera defined by the width and height of its canvas
   as well as its field of view, which is the solid angle limiting
   the camera's perspective. The distance between the eye and the canvas
   is 1.0 world unit."
  ([pixel-width pixel-height field-of-view]
    (make-camera pixel-width pixel-height field-of-view I₄))
  ([pixel-width pixel-height field-of-view transform]
    (let [half-view    (Math/tan (/ field-of-view 2.0))
          aspect-ratio (/ pixel-width pixel-height)
          ;; Note that we associate these two properties with the camera itself
          ;; to avoid recomputing them every time we call `pixel-size-for`
          ;; and/or `ray-for`
          [half-world-width half-world-height] (if (>= aspect-ratio 1)
                                                 [half-view (/ half-view aspect-ratio)]
                                                 [(* half-view aspect-ratio) half-view])]
  {:pixel-width       pixel-width
   :half-world-width  half-world-width
   :pixel-height      pixel-height
   :half-world-height half-world-height
   :field-of-view     field-of-view
   :transform         transform})))

(defn pixel-size-for
  "Computes the size of a pixel in world units for the given camera.
   Note that pixels are always square so we only need to compute
   the size of one dimension."
  [{:keys [half-world-width pixel-width] :as camera}]
  (/ (* half-world-width 2.0) pixel-width))
