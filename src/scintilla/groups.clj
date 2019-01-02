(ns scintilla.groups
  (:require [scintilla.matrix :refer [I₄]]))

(defn make-group
  ([]
   (make-group I₄))
  ([transform]
   {:object-type :group
    :objects     []
    :parent      nil
    :transform   transform}))

(defn add-children
  [group objects]
  (let [new-child-objects (map #(assoc-in % [:parent] group) objects)]
    (update-in group [:objects] concat new-child-objects)))
