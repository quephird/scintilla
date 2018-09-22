(ns scintilla.io)

(defn ppm-header
  [canvas]
  (let [height (count canvas)
        width  (count (first canvas))]
    (format "P3\n%s %s\n255" width height)))
