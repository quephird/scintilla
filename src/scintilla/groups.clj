(ns scintilla.groups
  (:require [scintilla.matrix :refer [I₄] :as m]))

(declare transform-child)
(declare transform-group)

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
  (transform-group group new-transform))

(defn transform-group
  "Recursively applies the transform to each child object in the group."
  [{:keys [children] :as group} transform]
  (let [transformed-children (map #(transform-child % transform) children)]
    (assoc-in group [:children] transformed-children)))

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
   (let [new-group {:object-type :group
                    :children    objects}]
     (transform-group new-group transform))))

(defn add-children
  "Convenience function to append the new objects to the extant list
   of children in the group."
  [group new-objects]
  (update-in group [:children] concat new-objects))

;; TODO: Need to improve the API here for allowing
;;       bulk updating of certain properties like:
;;
;;          set-material
;;          set-color
