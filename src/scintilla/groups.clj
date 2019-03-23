(ns scintilla.groups
  (:require [scintilla.matrix :refer [I₄] :as m]
            [scintilla.numeric :refer [ε]]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

;; TODO: Need to improve the API here for allowing
;;       bulk updating of certain properties like:
;;
;;          set-material
;;          set-color

(declare eight-corners-for)
(declare make-bounding-box)

(defmulti eight-corners-for :object-type)

(defmethod eight-corners-for :group
  [{:keys [children]}]
  (let [bounding-box (make-bounding-box children)]
    (s/eight-corners-for bounding-box)))

(defmethod eight-corners-for :shape
  [shape]
  (s/eight-corners-for shape))

(defmethod eight-corners-for :default
  [_]
  nil)

(defn make-bounding-box
  [objects]
  (if (empty? objects)
    ;; This is a bit of a hack to insure that we _always_
    ;; make a bounding box, even for an empty group. That way
    ;; we avoid having to nil-check everything downstream.
    (s/make-cube {:transform (t/scaling-matrix ε ε ε)})
    (let [all-corners       (mapcat eight-corners-for objects)
          bottom-left-front (apply map min all-corners)
          top-right-back    (apply map max all-corners)
          [sx sy sz _]      (map #(* 0.5 (- %1 %2)) top-right-back bottom-left-front)
          [tx ty tz _]      (map #(* 0.5 (+ %1 %2)) top-right-back bottom-left-front)
          transform         (m/matrix-times
                             (t/translation-matrix tx ty tz)
                             (t/scaling-matrix sx sy sz))]
      (s/make-cube {:transform transform}))))

(declare transform-child)
(declare transform-children)

(defmulti transform-child
  "This multimethod either pre-multiplies the transform of the
   given object if it is a shape by the new transform passed in,
   or otherwise recurses by transforming the entire group."
  (fn [{:keys [object-type] :as object} _]
    object-type))

(defmethod transform-child :shape
  [{:keys [transform] :as shape} new-transform]
  (let [transform' (m/matrix-times new-transform transform)]
    (-> shape
        (assoc-in [:transform] transform'))))

(defmethod transform-child :group
  [{:keys [children] :as group} new-transform]
  (let [new-children (transform-children children new-transform)
        bounding-box (make-bounding-box new-children)]
    (-> group
        (assoc-in [:children] new-children)
        (assoc-in [:bounding-box] bounding-box))))

(defn transform-children
  "Recursively applies the transform to each child object in the group."
  [children transform]
  (map #(transform-child % transform) children))

(defn transform-group
  "Convenience function."
  [{:keys [children] :as group} transform]
  (let [new-children     (transform-children children transform)
        new-bounding-box (make-bounding-box new-children)]
    (-> group
        (assoc-in [:children] new-children)
        (assoc-in [:bounding-box] new-bounding-box))))

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
         bounding-box        (make-bounding-box transformed-objects)
         ]
     {:object-type  :group
      :children     transformed-objects
      :bounding-box bounding-box
      })))

(defn add-children
  "Convenience function to append the new objects to the extant list
   of children in the group."
  [group new-objects]
  ;; TODO: Need to recompute bounding box
  (update-in group [:children] concat new-objects))
