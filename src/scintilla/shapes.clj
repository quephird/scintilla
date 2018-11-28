(ns scintilla.shapes
  (:require [scintilla.materials :as a]
            [scintilla.matrix :refer [I₄] :as m]
            [scintilla.tuple :as u]))

(defn make-sphere
  ([]
    (make-sphere a/default-material))
  ([material]
    (make-sphere material I₄))
  ([material transform]
    {:shape-type :sphere
     :material material
     :matrix transform}))

(defmulti normal-for (fn [shape _] (:shape-type shape)))

;; TODO: Look into how to better do this in a less hacky way.
(defmethod normal-for :sphere
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
