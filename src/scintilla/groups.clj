(ns scintilla.groups
  (:require [scintilla.matrix :refer [I₄] :as m]
            [scintilla.ray :as r]
            [scintilla.shapes :as s]))

(defn make-group
  ([]
   (make-group []))
  ([objects]
   (make-group objects I₄))
  ([objects transform]
   ;; TODO: update parents in child objects
   {:objects  objects
    :transform transform}))
  
(defn add-child
  "Note that in order to preserve immutability and transactional
   integrity, we return new versions of _both_ entities here."
  [group object]
  {:group  (update-in group [:objects] conj object)
   :object (assoc-in object [:parent] group)})

(defn all-intersections-for
  "Returns the set of all intersections that the given ray
   makes with the set of objects in the given group."
  [{:keys [objects transform] :as group} ray]
  (let [local-ray (r/transform ray (m/inverse transform))]
    (->> objects
         (map #(s/intersections-for % local-ray))
         (apply concat)
         (sort-by :t))))
