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

(defn make-empty-parse-results
  "This structure tracks the set of vertices as the file
   is being parsed, as well as the set of parse group names
   and their respective triangles. It also is set up to
   track the current group name."
  []
  {:vertices      []
   :groups        {:default []}
   :current-group :default})

(defmulti parse-line
  "Takes a line read in from an OBJ file as well as
   the set of all objects parsed up until this point
   and returns the new set of objects if the line
   can be parsed. This multimethod dispatches on the
   first word on the line, representing an object type."
  (fn [line _]
    (-> line (str/split #" ") first)))

;; Vertex statement
(defmethod parse-line "v"
  [line objects]
  (let [[_ & args] (str/split line #" ")
        point      (vec (map #(Double/parseDouble %) args))]
    (update-in objects [:vertices] conj point)))

;; Face statement
(defmethod parse-line "f"
  [line parser-results]
  (let [[_ & vertices] (str/split line #" ")
        [starting-index & other-indices] (vec (map #(Integer/parseInt %) vertices))
        vertex-pairs   (partition 2 1 other-indices)
        new-triangles  (mapv #(into [] (cons starting-index %)) vertex-pairs)
        current-group  (:current-group parser-results)]
    (update-in parser-results [:groups current-group] into new-triangles)))

;; Comment
(defmethod parse-line "#"
  [_ objects]
  objects)

(defmethod parse-line :default
  [line objects]
  (if (empty? line)
    objects
    (throw (ex-info "Cannot parse line"
                    {:causes #{:invalid-command}}))))

(defn parse-file
  "Takes a path to an OBJ file and returns a set of parsed
   objects in a hashmap."
  [filename]
  (with-open [reader (io/reader (io/resource filename))]
    (let [lines (line-seq reader)]
      (loop [results (make-empty-parse-results)
             [current & remaining] lines]
        (let [new-results (parse-line current results)]
          (if (empty? remaining)
            new-results
            (recur new-results remaining)))))))
