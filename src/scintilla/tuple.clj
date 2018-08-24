(ns scintilla.tuple)

(defn point?
  [[_ _ _ w]]
  (= 1 w))

(defn vector?
  [[_ _ _ w]]
  (= 0 w))

(defn make-point
  [x y z]
  [x y z 1])

(defn make-vector
  [x y z]
  [x y z 0])
