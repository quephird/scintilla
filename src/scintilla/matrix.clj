(ns scintilla.matrix)

(def I
  [[1 0 0 0]
   [0 1 0 0]
   [0 0 1 0]
   [0 0 0 1]])

(defn transpose
  [m]
  (let [column-count (count (first m))]
    (mapv (fn [column-idx]
            (mapv (fn [row]
                    (get row column-idx)) m)) (range column-count))))

(defn matrix-times
  [matrix1 matrix2]
  (mapv (fn [row1]
          (mapv (fn [column2]
                  (reduce + 0 (map * row1 column2)))
                (transpose matrix2)))
        matrix1))

(defn tuple-times
  [matrix tuple]
  (mapv (fn [row]
          (mapv (fn [element]
                  (reduce + 0 (map * row tuple)))
                tuple))
        matrix))

(defn scalar-times
  [matrix scalar]
  (mapv (fn [row]
          (mapv (fn [cell] (* scalar cell)) row)) matrix))

(defn remove-at-index
  [index vector]
  (into (subvec vector 0 index) (subvec vector (inc index) (count vector))))

(defn submatrix
  [row-idx column-idx matrix]
  (->> matrix
       (mapv (fn [row] (remove-at-index column-idx row)))
       (remove-at-index row-idx)))

(defn sign
  [row-idx column-idx]
  (if (even? (+ row-idx column-idx))
    1
    -1))

(declare minor)
(declare determinant)

(defn minor
  [row-idx column-idx matrix]
  (let [matrix' (submatrix row-idx column-idx matrix)]
    (determinant matrix')))

(defn cofactor
  [row-idx column-idx matrix]
  (* (sign row-idx column-idx)
     (minor row-idx column-idx matrix)))

(defn determinant
  [matrix]
  (case (count matrix)
    2
      (let [[[a b] [c d]] matrix]
        (- (* a d) (* b c)))
    (reduce (fn [acc column-idx]
              (+ acc (* (get (first matrix) column-idx)
                        (cofactor 0 column-idx matrix))))
            0
            (range (count matrix)))))
