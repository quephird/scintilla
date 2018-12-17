(ns scintilla.scene
  (:require [scintilla.lighting :as l]
            [scintilla.matrix :as m]
            [scintilla.transformation :as t]
            [scintilla.tuple :as u]))

(defn make-scene
  ([]
    (make-scene []))
  ([objects]
    (make-scene objects l/default-light))
  ([objects light]
    {:objects objects
     :light light}))

(defn add-objects
  [scene objects]
  (update-in scene [:objects] concat objects))
