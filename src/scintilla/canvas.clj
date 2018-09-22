(ns scintilla.canvas)

(defn make-canvas
  [width height]
  (vec (repeat height (vec (repeat width [0 0 0])))))

(defn write-pixel
  [canvas x y color]
  (assoc-in canvas [y x] color))

(defn read-pixel
  [canvas x y]
  (get-in canvas [y x]))
