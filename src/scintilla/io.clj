(ns scintilla.io
  (:require [scintilla.color :as color]))

(defn ppm-header
  [canvas]
  (let [height (count canvas)
        width  (count (first canvas))]
    (format "P3\n%s %s\n255" width height)))

(defn- new-line-length
  [acc & new-color-components]
  (+ (count (last (clojure.string/split-lines acc)))
     (count (clojure.string/join " " (map str new-color-components)))))

(defn- format-row
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
  [canvas]
  (let [height (count canvas)
        width  (count (first canvas))]
    (->> (for [row canvas] (format-row row))
         (interpose "\n")
         (apply str))))

(defn save-as-ppm
  [canvas filename]
  (let [header (ppm-header canvas)
        body   (ppm-body canvas)]
    (with-open [w (clojure.java.io/writer filename)]
      (.write w header)
      (.write w "\n")
      (.write w body)
      (.write w "\n"))))
