(ns scintilla.groups
  (:require [scintilla.matrix :refer [I₄] :as m]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

;; TODO: Need to improve the API here for allowing
;;       bulk updating of certain properties like:
;;
;;          set-material
;;          set-color

(defn make-bounding-box
  [points]
  (let [bottom-left-front (apply map min points)
        top-right-back    (apply map max points)
        [sx sy sz _]      (map - top-right-back bottom-left-front)
        [tx ty tz _]      (map #(* 0.5 (+ %1 %2)) top-right-back bottom-left-front)
        transform         (m/matrix-times
                           (t/translation-matrix tx ty tz)
                           (t/scaling-matrix sx sy sz))]
    (s/make-cube {:transform transform})))

(declare transform-child)
(declare transform-children)

(defmulti transform-child
  "This multimethod either pre-multiplies the transform of the
   given object if it is a shape by the new transform passed in,
   or otherwise recurses by transforming the entire group."
  (fn [{:keys [object-type] :as object} _]
    object-type))

(defmethod transform-child :shape
  [shape new-transform]
  (update-in shape [:transform] (fn [old-transform]
                                    (m/matrix-times new-transform old-transform))))

(defmethod transform-child :group
  [group new-transform]
  (update-in group [:children] transform-children new-transform))

(defn transform-children
  "Recursively applies the transform to each child object in the group."
  [children transform]
  (map #(transform-child % transform) children))

(defn make-group
  "The approach here is much different from the one in the book.
   Instead of maintaining bidirectional references between parents and
   children, which is not easily possible using raw Clojure maps
   without resorting to some sort of tracking of IDs, we simply
   maintain a tree of maps. Any time a group is transformed, that
   matrix is immediately 'pushed' down the entire tree of shapes,
   and pre-multiplied with the existing transform matrix on each.
   We can get away with this here because there is no need for any
   ad hoc navigation around the tree, or any other requirement to
   select or 'mutate' any portion of the tree, or differentiate
   between shape space and its group space."
  ([objects]
   (make-group objects I₄))
  ([objects transform]
   (let [transformed-objects (transform-children objects transform)
         ]
     ;; Need to compute and assoc in bounding box
     {:object-type :group
      :children    transformed-objects})))

(defn add-children
  "Convenience function to append the new objects to the extant list
   of children in the group."
  [group new-objects]
  ;; Need to recompute bounding box
  (update-in group [:children] concat new-objects))
