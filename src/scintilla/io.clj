(ns scintilla.io
  (:require [scintilla.color :as color]))

(defn ppm-header
  "Takes a canvas and produces a string representation of the
   header of the standard PPM file format."
  [canvas]
  (let [height (count canvas)
        width  (count (first canvas))]
    (format "P3\n%s %s\n255" width height)))

(defn- new-line-length
  "Helper function for determining length of a new line of text
   for the given accumulated PPM body and a variable number of
   color components for the next pixel being written out."
  [current-ppm-body & new-color-components]
  (+ (count (last (clojure.string/split-lines current-ppm-body)))
     (count (clojure.string/join " " (map str new-color-components)))))

(defn- format-row
  "Helper function for producing one or more lines of text representing
   a single row of pixels, adhering to the PPM file format specification."
  [row]
  (->> row
       (reduce (fn [acc color]
         (let [[r g b] (map color/clamp-and-scale color)]
           (cond
             (empty? acc)
               (format "%s %s %s" r g b)
             (< (new-line-length acc r g b) 70)
               (format "%s %s %s %s" acc r g b)
             (< (new-line-length acc r g) 70)
               (format "%s %s %s\n%s" acc r g b)
             (< (new-line-length acc r) 70)
               (format "%s %s\n%s %s" acc r g b)
             :else
               (format "%s\n%s %s %s" acc r g b)))) "")))

(defn ppm-body
  "Takes a canvas and produces a string representation of the
   body of the standard PPM file format."
  [canvas]
  (let [height (count canvas)
        width  (count (first canvas))]
    (->> (for [row canvas] (format-row row))
         (interpose "\n")
         (apply str))))

(defn save-as-ppm
  "Main function to write out the contents of the canvas to a PPM file."
  [canvas filename]
  (let [header (ppm-header canvas)
        body   (ppm-body canvas)]
    (with-open [w (clojure.java.io/writer filename)]
      (.write w header)
      (.write w "\n")
      (.write w body)
      (.write w "\n"))))
