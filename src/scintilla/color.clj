(ns scintilla.color)

(defn add
  [& vs]
  (apply mapv + vs))

(defn subtract
  [v1 v2]
  (mapv - v1 v2))

(defn scalar-times
  [v s]
  (mapv #(* s %) v))

(defn hadamard-product
  [v1 v2]
  (mapv * v1 v2))

(defn clamp-and-scale
  "Takes a color represented by RGB values, intended to be in the
   range [0.0, 1.0], clamps them to that interval, and scales them to
   be in the range [0.0, 255.0]."
  [c]
  (-> (cond (< c 0.0) 0.0 (> c 1.0) 1.0 :else c)
      (clojure.core/* 255.0)
      Math/round))
