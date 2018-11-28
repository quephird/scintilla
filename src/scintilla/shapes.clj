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

(defmulti local-normal-for (fn [shape _] (:shape-type shape)))

(defmethod local-normal-for :sphere
  [_ local-point]
  (u/subtract local-point [0.0 0.0 0.0 1.0]))

(defmethod local-normal-for :plane
  [_ _]
  [0 1 0 0])

(defn normal-for
  "This is the 'public' interface for computing the normal
   vector for any arbitrary type of shape. It first converts
   the world point to a local point, computes the normal in
   that coordinate system by deferring the specialized
   implementation for the shape, then transforms it back to the
   world coordinate system."
  [{:keys [matrix] :as shape} world-point]
  (let [local-normal (as-> matrix $
                           (m/inverse $)
                           (m/tuple-times $ world-point)
                           (local-normal-for shape $))]
    (-> matrix
        m/inverse
        m/transpose
        (m/tuple-times local-normal)
        (assoc 3 0)  ;; TODO: This is a hack per the book; look for better way
        u/normalize)))

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
