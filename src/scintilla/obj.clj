(ns scintilla.obj
  (:require [scintilla.groups :as g]
            [scintilla.mtl :as mf]
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
  {:vertices         []
   :normals          []
   :groups           {:default []}
   :materials        {}
   :current-group    :default
   :current-material nil})

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

;; Material file reference statement
(defmethod parse-line "mtllib"
  [line parser-results]
  (let [[_ filename] (str/split line #"\s+")
        materials    (-> filename
                         mf/parse-file
                         :materials)]
    (update-in parser-results [:materials] merge materials)))

;; Material file reference statement
(defmethod parse-line "usemtl"
  [line parser-results]
  (let [[_ name] (str/split line #"\s+")
        material (keyword name)]
    (assoc-in parser-results [:current-material] material)))

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

(defn- third
  "Useful convenience function"
  [list]
  (-> list rest rest first))

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

(defn make-triangle-maps-for
  "Helper function to take a list of vertex indices and list of normal
   indices for a single face, and return a list of maps of triplets of
   indices representing each triangle that composes the face.

   The strategy used here is to create a fan of triangles with the
   first index for each list shared across all triangles as the
   starting point. Consider the following face statement:

      f 1//6 2//5 3//4 4//3 5//2 6//1

   This is assumed to represent a hexagonal face, and so we need to
   produce four triangles to 'cover' the face like this:

                   2 ____________ 3
                    /         ,-'﹨
                   /      ,-'     ﹨
                  /   ,-'          ﹨
              1  /,_'_______________﹨ 4
                 ﹨'-,               /
                  ﹨   '-,          /
                   ﹨      '-,     /
                    ﹨         '-,/
                   6 ‾‾‾‾‾‾‾‾‾‾‾‾ 5

   Specifically, we want to produce triples of vertices like:

      [1 2 3] [1 3 4] [1 4 5] [1 5 6]

   Additionally, we need to produce the corresponding list of normal
   vector indices like so:

      [6 5 4] [6 4 3] [6 3 2] [6 2 1]

   Putting it all together, the desired return value of this function is

      [{:vertices [1 2 3] :normals [6 5 4]}
       {:vertices [1 3 4] :normals [6 4 3]}
       {:vertices [1 4 5] :normals [6 3 2]}
       {:vertices [1 5 6] :normals [6 2 1]}]
  "
  [[v1 & vs :as vertex-indices]
   [n1 & ns :as normal-indices]
   current-material]
  (let [vertex-pairs   (partition 2 1 vs)
        vertex-triples (mapv #(into [] (cons v1 %)) vertex-pairs)
        triangle-maps  (map (fn [vt]
                              {:vertices vt
                               :material current-material}) vertex-triples)]
    (if (some nil? normal-indices)
      triangle-maps
      (let [normal-pairs   (partition 2 1 ns)
            normal-triples (mapv #(into [] (cons n1 %)) normal-pairs)]
        (map (fn [triangle-map nt]
               (assoc triangle-map :normals nt)) triangle-maps normal-triples)))))

;; Face statement
(defmethod parse-line "f"
  [line parser-results]
  (let [[_ & vertex-data] (str/split line #"\s+")
        all-indices       (parse-all-indices vertex-data)
        vertex-indices    (map first all-indices)
        normal-indices    (map third all-indices)
        current-material  (:current-material parser-results)
        new-triangle-maps (make-triangle-maps-for vertex-indices
                                                  normal-indices
                                                  current-material)
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
  "Takes a triple of triangle indices, and the list of all vertices and
   all normals and either returns a Scintilla triangle object or
   smooth triangle object if vertex normals are specified."
  [{:keys [vertices normals material] :as group-data}
   {all-vertices  :vertices
    all-normals   :normals
    all-materials :materials :as results}]
  (let [vertex-points (->> vertices
                           (map dec)
                           (map #(get all-vertices %))
                           (map #(conj % 1)))
        material      (get-in all-materials [material])]
    (if (nil? normals)
      (s/make-triangle vertex-points :material material)
      (let [normal-vectors (->> normals
                                (map dec)
                                (map #(get all-normals %))
                                (map #(conj % 0)))]
        (s/make-smooth-triangle vertex-points
                                normal-vectors
                                :material material)))))

(defn- make-group-for
  "Takes a set of parsed data for a group, named or default, and
   the list of all vertices, and returns a Scintilla group object."
  [group-data results]
  (let [triangles (map #(make-triangle-for % results) group-data)]
    (g/make-group triangles)))

(defn results->groups
  "Takes the complete set of parsed vertices, textures, groups, etc.
   and returns a Scintilla group containing all objects represented
   in the OBJ file."
  [{:keys [groups] :as results}]
  (let [named-groups (map (fn [[group-name group-data]]
                            (make-group-for group-data results)) groups)]
    (g/make-group named-groups)))

(defn load-obj-file
  "Intended to be the main public interface to this namespace, this
   takes a filename for an OBJ file, parses it entirely, and returns
   the Scintilla representation of the entire scene in the file."
  [filename]
  (-> filename
      parse-file
      results->groups))
