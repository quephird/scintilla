(ns scintilla.canvas)

(defn make-canvas
  "Creates a data structure that contains pixel data, for now just
   the RGB color values, to represent a graphical canvas."
  [width height]
  (vec (repeat height (vec (repeat width [0 0 0])))))

(defn write-pixel
  "Takes a canvas, the x and y coordinates of a target pixel,
   and a color, and returns a new canvas with that pixel to the
   the color passed in."
  [canvas x y color]
  (assoc-in canvas [y x] color))

(defn read-pixel
  "Returns the color of the pixel, identified by the x and y
   coordinates, of the canvas passed in."
  [canvas x y]
  (get-in canvas [y x]))
