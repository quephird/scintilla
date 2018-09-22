(ns scintilla.canvas)

(defn make-canvas
  [x y]
  (into {}
    (for [i (range x) j (range y)]
      [[i j] [0 0 0]])))

(defn write-pixel
  [canvas x y color]
  (assoc-in canvas [[x y]] color))

(defn read-pixel
  [canvas x y]
  (get-in canvas [[x y]]))
