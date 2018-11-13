(ns scintilla.scene)

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

;; TODO: Consider not using protocols for vector types;
;; they make this code too verbose.
;; TODO: Figure out how to speed this up; maybe resort to
;; a mutable array after all.
(defn pixel->scene
  "Converts the (x,y) coordinates in the canvas system
   to (x,y,z) coordinates of the scene world system"
  [[pixel-x pixel-y :as pixel]
   [canvas-w canvas-h :as canvas-dimensions]
   [wall-x wall-y wall-z _ :as wall-point]
   [wall-w wall-h :as wall-dimensions]]
   (let [scene-x (clojure.core/- (clojure.core/* wall-w (clojure.core// pixel-x canvas-w)) (clojure.core// wall-w 2.0))
         scene-y (clojure.core/- (clojure.core/* wall-h (clojure.core// pixel-y canvas-h)) (clojure.core// wall-h 2.0))]
     [scene-x scene-y wall-z 1.0]))
