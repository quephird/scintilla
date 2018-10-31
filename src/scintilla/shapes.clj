(ns scintilla.shapes)

(defn make-sphere
  [center radius]
  {:shape-type :sphere
   :shape-center center
   :radius radius})
