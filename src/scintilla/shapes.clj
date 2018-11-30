(ns scintilla.shapes
  (:require [scintilla.materials :as a]
            [scintilla.matrix :refer [I₄] :as m]
            [scintilla.tuple :as u]))

(defn make-shape
  ([shape-type]
    (make-shape shape-type a/default-material))
  ([shape-type material]
    (make-shape shape-type material I₄))
  ([shape-type material transform]
    {:shape-type shape-type
     :material material
     :matrix transform}))

(defn make-sphere
  "The default sphere is centered at the world origin
   and has radius 1."
  [& args]
  (apply make-shape :sphere args))

(defn make-plane
  "The default plane lies in the 𝑥𝑧 plane."
  [& args]
  (apply make-shape :plane args))
