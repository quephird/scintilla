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
   {:object-type :group
    :objects     objects
    :transform   transform}))
