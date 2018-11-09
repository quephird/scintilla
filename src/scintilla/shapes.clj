(ns scintilla.shapes
  (:require [scintilla.materials :as a]
            [scintilla.matrix :refer :all]
            [scintilla.tuple :refer :all]))

(def default-color [1 0 0])

(defn make-sphere
  ([]
    (make-sphere default-color))
  ([color]
    (make-sphere color I₄))
  ([color matrix]
    (make-sphere color matrix a/default-material))
  ([color matrix material]
    {:shape-type :sphere
     :material material
     :matrix matrix
     :color color}))

(defmulti find-normal (fn [shape _] (:shape-type shape)))

;; TODO: Look into how to better do this in a less hacky way.
(defmethod find-normal :sphere
  [{:keys [matrix]} world-point]
  (let [object-point (-> matrix
                         inverse
                         (tuple-times world-point))
        object-normal (- object-point [0.0 0.0 0.0 1.0])]
    (-> matrix
        inverse
        transpose
        (tuple-times object-normal)
        (assoc 3 0)  ;; Per the book, this is a hack
        normalize)))

(defn find-reflection
  [in-vector normal-vector]
  (->> normal-vector
       (⋅ in-vector)
       (clojure.core/* 2.0)
       (* normal-vector)
       (- in-vector)))
