(ns scintilla.shapes
  (:require [scintilla.materials :as a]
            [scintilla.matrix :refer [I₄] :as m]
            [scintilla.patterns :as p]
            [scintilla.tuple :as u]))

;; We need a way to uniquely identify shapes, but since
;; we are using raw maps and not instantiating and referengin
;; objects, we need an explicit strategy, and so we use UUIDs.
(defn make-shape
  ([shape-type]
    (make-shape shape-type a/default-material))
  ([shape-type material]
    (make-shape shape-type material I₄))
  ([shape-type material transform]
   {:id         (java.util.UUID/randomUUID)
    :shape-type shape-type
    :material   material
    :matrix     transform}))

(defn make-sphere
  "The default sphere is centered at the world origin
   and has radius 1."
  [& args]
  (apply make-shape :sphere args))

(defn make-plane
  "The default plane lies in the 𝑥𝑧 plane."
  [& args]
  (apply make-shape :plane args))

(defn color-for
  "This function either returns the simple color for the
   entire hit shape or defers computation of the color to the
   shape's pattern implementation itself if it exists
   for the surface point in question."
  [prepared-hit]
  (let [{:keys [pattern color]} (get-in prepared-hit [:shape :material])]
    (if (nil? pattern)
      color
      (p/color-for prepared-hit))))
