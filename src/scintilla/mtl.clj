(ns scintilla.mtl
  (:require [scintilla.groups :as g]
            [scintilla.shapes :as s]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn make-empty-parse-results
  []
  {:materials {}
   :current-material nil})

(defn- statement-type-for
  "Dispatcher for `parse-line` multimethod."
  [line]
  (-> line (str/split #" ") first))

(defmulti parse-line
  "Takes a line read in from an MTL file as well as
   the set of all materials parsed up until this point
   and returns the new set of meterials if the line
   can be parsed, otherwise returns the original parsed results
   untouched. This multimethod dispatches on the
   first word on the line, representing a statement type."
  (fn [line _]
    (statement-type-for line)))

;; New material statement
(defmethod parse-line "newmtl"
  [line parser-results]
  (let [[_ name & _] (str/split line #"\s+")
        new-material (keyword name)]
    (assoc-in parser-results [:current-material] new-material)))

(defn- parse-color-line
  [line color-type parser-results]
  (let [[_ & args]       (str/split line #"\s+")
        color            (vec (map #(Double/parseDouble %) args))
        current-material (:current-material parser-results)]
    (assoc-in parser-results [:materials current-material color-type] color)))

;; Ambient color statement
(defmethod parse-line "Ka"
  [line parser-results]
  (parse-color-line line :ambient parser-results))

;; Diffuse color statement
(defmethod parse-line "Kd"
  [line parser-results]
  (parse-color-line line :diffuse parser-results))

;; Specular color statement
(defmethod parse-line "Ks"
  [line parser-results]
  (parse-color-line line :specular parser-results))

;; Shininess statement
(defmethod parse-line "Ns"
  [line parser-results]
  (let [[_ & args]        (str/split line #"\s+")
        shininess         (-> args
                              first
                              (Double/parseDouble))
        current-material (:current-material parser-results)]
    (assoc-in parser-results [:materials current-material :shininess] shininess)))

;; Handle for all blank lines and other non-supported statements
(defmethod parse-line :default
  [_ parser-results]
  parser-results)

(defn parse-file
  [filename]
  (with-open [reader (io/reader (io/resource filename))]
    (let [lines (line-seq reader)]
      (loop [results (make-empty-parse-results)
             [current & remaining] lines]
        (let [new-results (parse-line current results)]
          (if (empty? remaining)
            new-results
            (recur new-results remaining)))))))
