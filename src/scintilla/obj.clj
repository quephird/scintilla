(ns scintilla.obj
  (:require [scintilla.groups :as g]
            [scintilla.shapes :as s]
            [clojure.java.io :as io]))

(defn load
  "Takes a path to an OBJ file and returns a Scintilla
   group structure representing the scene described in it."
  [filename]
  (with-open [reader (io/reader filename)]
    (let [lines (line-seq reader)]
      (loop [objects [] [current & remaining] lines]
        (if (empty? remaining)
          objects
          (recur (conj objects (count current)) remaining)))
      )))
  
