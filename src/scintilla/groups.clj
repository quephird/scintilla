(ns scintilla.groups
  (:require [scintilla.matrix :refer [I₄] :as m]))

(defn make-group
  ([]
   (make-group I₄))
  ([transform]
   {:object-type :group
    :objects     []
    :transform   transform
    :group-transform I₄}))

(defmulti update-group-transform
  (fn [child-object _] (:object-type child-object)))

(defmethod update-group-transform :group
  [{:keys [objects] :as child-group} group]
  (let [new-grandchildren (map #(update-group-transform % group) objects)]
    (-> child-group
        (update-in [:group-transform] m/matrix-times (:transform group))
        (assoc-in [:objects] new-grandchildren))))

(defmethod update-group-transform :shape
  [child-object group]
  (update-in child-object [:group-transform] m/matrix-times (:transform group)))

(defn add-children
  [group objects]
  (let [new-child-objects (map #(update-group-transform % group) objects)]
    (update-in group [:objects] concat new-child-objects)))
