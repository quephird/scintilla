(ns scintilla.camera)

(defn make-camera
  [width height field-of-view]
  {:width width
   :height height
   :field-of-view field-of-view})

(defn pixel-size-for
  "Computes the size of a pixel in world units for the given camera."
  [{:keys [width height field-of-view] :as camera}]
  (let [half-view  (Math/tan (/ field-of-view 2.0))
        aspect     (/ height width)
        [half-width half-height] (if (>= aspect 1)
                                     [half-view (/ half-view aspect)]
                                     [(* half-view aspect) half-view])]
    (/ (* half-width 2.0) height)))
