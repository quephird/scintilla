(ns scintilla.obj
  (:require [scintilla.groups :as g]
            [scintilla.shapes :as s]
            [clojure.java.io :as io]
            [clojure.string :as str]))

;; TODO: Fully describe the format of the Wavefront OBJ file
;;
;; vertex line
;;
;; * v 1 2 3
;;
;; face line
;;
;; * f 1 2 4
;;
;; 

(defmulti parse-line
  "Takes a line read in from an OBJ file as well as
   the set of all objects parsed up until this point
   and returns the new set of objects if the line
   can be parsed. This multimethod dispatches on the
   first word on the line, representing an object type."
  (fn [line _]
    (-> line (str/split #" ") first)))

(defmethod parse-line "v"
  [line objects]
  (let [[_ & args] (str/split line #" ")
        point      (vec (map #(Double/parseDouble %) args))]
    (update-in objects [:vertices] conj point)))

(defmethod parse-line "f"
  [line objects]
  (let [[_ & args]     (str/split line #" ")
        vertex-indices (vec (map #(Integer/parseInt %) args))]
    (update-in objects [:faces] conj vertex-indices)))

(defmethod parse-line :default
  [line objects]
  (if (empty? line)
    objects
    (throw (ex-info "Cannot parse line"
                    {:causes #{:invalid-command}}))))

(defn load
  "Takes a path to an OBJ file and returns a Scintilla
   group structure representing the scene described in it."
  [filename]
  (with-open [reader (io/reader (io/resource filename))]
    (let [lines (line-seq reader)]
      (loop [objects {:vertices [] :faces []}
             [current & remaining] lines]
        (let [new-objects (parse-line current objects)]
          (if (empty? remaining)
            new-objects
            (recur new-objects remaining))))
      )))
