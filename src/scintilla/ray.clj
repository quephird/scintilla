(ns scintilla.ray
  (:require [scintilla.matrix :as m]
            [scintilla.transformation :as t]
            [scintilla.tuple :as u]))

(defn make-ray
  "Constructs a data structure representing a ray"
  [point direction]
  {:point point
   :direction direction})

(defn position
  "Computes the position along the given ray parameterized by t."
  [{:keys [point direction] :as ray} t]
  (u/plus point (u/scalar-times direction t)))

(defn transform
  "NOTA BENE: note that a translation matrix applied to a vector
   is an effective no-op, but that a scaling matrix is not, which
   works to our advantage here because that is the desired behavior
   and we don't need to know what kind of transformation matrix
   is passed in."
  [{:keys [point direction]} matrix]
  (make-ray (m/tuple-times matrix point) (m/tuple-times matrix direction)))

(defn reflected-vector-for
  "Computes the vector that is the result of reflecting
   the in-vector around the normal vector."
  ;;
  ;;                    normal
  ;;                    vector
  ;;                г      ^       ⟋
  ;;                  ⟍    |    ⟋
  ;;  reflected vector  ⟍  |  ⟋ incident vector
  ;;                      ⟍|∟
  ;;                ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
  [in-vector normal-vector]
  (->> normal-vector
       (u/dot-product in-vector)
       (* 2.0)
       (u/scalar-times normal-vector)
       (u/subtract in-vector)))
