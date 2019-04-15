(ns scintilla.obj
  (:require [scintilla.groups :as g]
            [scintilla.shapes :as s]
            [clojure.java.io :as io]
            [clojure.string :as str]))

;; This namespace is responsible for parsing Wavefront OBJ files.
;; It currently supports the following statement types:
;;
;; vertex statement
;;
;;    v 1 2 3
;;
;; vertex normal statement
;;
;;    vn 0.1 0.7071 0.5
;;
;; face statement (just one of three possible variations
;;
;;    f 1 2 4
;;
;; group statement
;;
;;    g SomeGroupName
;;
;; comment
;; 
;;    # This is an example comment
;;
;; All other statement types are currently ignored.

(defn make-empty-parse-results
  "This structure tracks the set of vertices as the file
   is being parsed, as well as the set of parse group names
   and their respective triangles. It also is set up to
   track the current group name."
  []
  {:vertices      []
   :normals       []
   :groups        {:default []}
   :current-group :default})

(defn- statement-type-for
  "Dispatcher for `parse-line` multimethod."
  [line]
  (-> line (str/split #" ") first))

(defmulti parse-line
  "Takes a line read in from an OBJ file as well as
   the set of all objects parsed up until this point
   and returns the new set of objects if the line
   can be parsed, otherwise returns the parsed objects
   untouched. This multimethod dispatches on the
   first word on the line, representing a statement type."
  (fn [line _]
    (statement-type-for line)))

;; Vertex statement
(defmethod parse-line "v"
  [line parser-results]
  (let [[_ & args] (str/split line #"\s+")
        point      (vec (map #(Double/parseDouble %) args))]
    (update-in parser-results [:vertices] conj point)))

;; Vertex normal statement
(defmethod parse-line "vn"
  [line parser-results]
  (let [[_ & args] (str/split line #"\s+")
        point      (vec (map #(Double/parseDouble %) args))]
    (update-in parser-results [:normals] conj point)))

;; TODO: I really don't like relying on `nil`s here;
;;       is there a way to avoid them?
(defn- parse-all-indices
  "Helper function to parse all indices from the vertex data in a
   face statement. Vertex data can be in the following forms:

     * f 1 2 3 4
     * f 1//1 2//2 3//3 4//4
     * f 1/4/1 2/3/2 3/2/3 4/1/4

   ... and so this method breaks up a single string containing
   _just_ the vertex data and returns a list of lists of
   integers. Each sublist contains:

     * at a minimum the index into the list of vertices as the first element
     * an optional second element as the index into the list of vertex textures
     * and optional third element as the index into the list of vertex normals"
  [vertex-data]
  (->> vertex-data
       (map #(str/split % #"/"))
       (map (fn [index-group]
              (map #(if (empty? %)
                      nil
                      (Integer/parseInt %)) index-group)))))

;; TODO: Give example inputs and outputs
(defn make-triangle-maps-for
  "Helper function to take a list of vertex indices and list of normal
   indices for a single face, and return a list of maps of triplets of
   indices for each triangle in the face."
  [[v1 & vs :as vertex-indices] [n1 & ns :as normal-indices]]
  (let [vertex-pairs (partition 2 1 vs)
        vertex-triples (mapv #(into [] (cons v1 %)) vertex-pairs)]
    (map (fn [vt] {:vertices vt}) vertex-triples)))


    ;;     normal-pairs (partition 2 1 ns)
    ;;     normal-triples (mapv #(into [] (cons n1 %)) normal-pairs)]
    ;; (map (fn [vt nt]
    ;;        {:vertices vt :normals nt}) vertex-triples normal-triples)))

(defn- third
  "Useful convenience function"
  [list]
  (-> list rest rest first))

;; Face statement
(defmethod parse-line "f"
  [line parser-results]
  (let [[_ & vertex-data] (str/split line #"\s+")
        all-indices       (parse-all-indices vertex-data)
        vertex-indices    (map first all-indices)
        normal-indices    (map third all-indices)
        new-triangle-maps (make-triangle-maps-for vertex-indices normal-indices)
        current-group     (:current-group parser-results)]
    (update-in parser-results [:groups current-group] into new-triangle-maps)))

;; Group statement
(defmethod parse-line "g"
  [line parser-results]
  (let [[_ name & _] (str/split line #"\s+")
        new-group    (keyword name)]
    (assoc-in parser-results [:current-group] new-group)))

;; Handle for all blank lines and other non-supported statements
(defmethod parse-line :default
  [_ parser-results]
  parser-results)

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

(defn- make-triangle-for
  "Takes a triple of triangle indices, and the list of all vertices,
   and returns a Scintilla triangle object."
  [{triangle-vertices :vertices} vertices]
  (let [[p1 p2 p3] (->> triangle-vertices
                        (map dec)
                        (map #(get vertices %))
                        (map #(conj % 1)))]
    (s/make-triangle p1 p2 p3)))

(defn- make-group-for
  "Takes a set of parsed data for a group, named or default, and
   the list of all vertices, and returns a Scintilla group object."
  [group-data vertices]
  (let [triangles (map #(make-triangle-for % vertices) group-data)]
    (g/make-group triangles)))

(defn results->groups
  "Takes the complete set of parsed vertices, textures, groups, etc.
   and returns a Scintilla group containing all objects represented
   in the OBJ file."
  [{:keys [groups vertices] :as results}]
  (let [named-groups (map (fn [[group-name group-data]]
                            (make-group-for group-data vertices)) groups)]
    (g/make-group named-groups)))

(defn load-obj-file
  "Intended to be the main public interface to this namespace, this
   takes a filename for an OBJ file, parses it entirely, and returns
   the Scintilla representation of the entire scene in the file."
  [filename]
  (-> filename
      parse-file
      results->groups))
