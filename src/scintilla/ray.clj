(ns scintilla.ray
  (:require [scintilla.camera :as c]
            [scintilla.matrix :as m]
;            [scintilla.shapes :as s]
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


;; TODO: Move to scintilla.camera
(defn ray-for
  "Computes the ray for the given camera and (x,y) coordinates of its canvas,
   in terms of the coordinate system correspondent with the inverse
   of the camera's transform matrix."
  [{:keys [half-world-width half-world-height transform] :as camera} x y]
  (let [pixel-size (c/pixel-size-for camera)
        [offset-x offset-y] (map #(* (+ % 0.5) pixel-size) [x y])
        [world-x world-y]   (map - [half-world-width half-world-height] [offset-x offset-y])
        inverse-transform   (m/inverse transform)
        point'              (m/tuple-times inverse-transform [world-x world-y -1 1])
        origin'             (m/tuple-times inverse-transform [0 0 0 1])
        direction'          (u/normalize (u/subtract point' origin'))]
    (make-ray origin' direction')))

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
