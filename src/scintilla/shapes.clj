(ns scintilla.shapes
  (:require [scintilla.materials :as a]
            [scintilla.matrix :as m]
            [scintilla.tuple :as u]))

(def default-color [1 0 0])

(defn make-sphere
  ([]
    (make-sphere default-color))
  ([color]
    (make-sphere color m/I₄))
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
                         m/inverse
                         (m/tuple-times world-point))
        object-normal (u/subtract object-point [0.0 0.0 0.0 1.0])]
    (-> matrix
        m/inverse
        m/transpose
        (m/tuple-times object-normal)
        (assoc 3 0)  ;; Per the book, this is a hack
        u/normalize)))

(defn find-reflection
  [in-vector normal-vector]
  (->> normal-vector
       (u/dot-product in-vector)
       (* 2.0)
       (u/scalar-times normal-vector)
       (u/subtract in-vector)))
