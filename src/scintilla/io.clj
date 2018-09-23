(ns scintilla.io)

(defn clamp-and-scale
  [c]
  (-> (cond (< c 0.0) 0.0 (> c 1.0) 1.0 :else c)
      (* 255.0)
      Math/round))

(defn ppm-header
  [canvas]
  (let [height (count canvas)
        width  (count (first canvas))]
    (format "P3\n%s %s\n255" width height)))

(defn ppm-color
  [color]
  (apply format "%s %s %s" (map clamp-and-scale color)))

(defn ppm-body
  [canvas]
  (let [height (count canvas)
        width  (count (first canvas))]
    (->> (for [row canvas]
           (->> (for [pixel row] (ppm-color pixel))
                (interpose " ")
                (apply str)))
         (interpose "\n")
         (apply str))))
