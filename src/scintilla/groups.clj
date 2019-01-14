(ns scintilla.groups
  (:require [scintilla.matrix :refer [I₄] :as m]))

(declare transform-child)
(declare transform-group)

(defmulti transform-child
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
  [{:keys [children] :as group} transform]
  (let [transformed-children (map #(transform-child % transform) children)]
    (assoc-in group [:children] transformed-children)))

(defn make-group
  "This function does _two_ things: maintaining a set of objects
   which should be transformed together, as well as modifying the
   individual transforms of each child object. This is done instead
   of maintaining bidirectional references between parents and
   children, which is not easily possible using raw Clojure maps
   without resorting to some sort of tracking of IDs. This proves
   to be a much simpler, and pure, way of accomplishing the same goals."
  ([objects]
   (make-group objects I₄))
  ([objects transform]
   (let [new-group {:object-type :group
                    :children    objects}]
     (transform-group new-group transform))))

(defn add-children
  [group new-objects]
  (update-in group [:children] concat new-objects))

;; TODO: Need to improve the API here for allowing
;;       bulk updating of certain properties like:
;;
;;          set-material
;;          set-color
