(ns scintilla.matrix)

(def I₄
  "The 4X4 identity matrix."
  [[1 0 0 0]
   [0 1 0 0]
   [0 0 1 0]
   [0 0 0 1]])

(defn transpose
  "Flips a square matrix over its diagonal."
  [m]
  (let [column-count (count (first m))]
    (mapv (fn [column-idx]
            (mapv (fn [row]
                    (get row column-idx)) m)) (range column-count))))

(defn matrix-times
  "Multiplies an l ⨯ m matrix by an m ⨯ n matrix,
   yielding an l ⨯ n matrix."
  [matrix1 matrix2]
  (mapv (fn [row1]
          (mapv (fn [column2]
                  (reduce + 0 (map * row1 column2)))
                (transpose matrix2)))
        matrix1))

(defn tuple-times
  "Multiplies an m ⨯ n matrix to a 1 ⨯ m tuple,
   yielding a 1 ⨯ m tuple."
  [matrix tuple]
  (mapv (fn [row]
          (reduce + 0 (map * row tuple)))
        matrix))

(defn scalar-times
  "Multiplies each element of an m ⨯ n matrix by a scalar value,
   yielding an m ⨯ n matrix."
  [matrix scalar]
  (mapv (fn [row]
          (mapv (fn [cell] (* scalar cell)) row)) matrix))

(defn scalar-divide
  "Divides each element of an m ⨯ n matrix by a scalar value,
   yielding an m ⨯ n matrix."
  [matrix scalar]
  (scalar-times matrix (/ 1.0 scalar)))

(defn- remove-at-index
  "Helper function to return a new vector with the element
   at the specified index removed."
  [index vector]
  (into (subvec vector 0 index) (subvec vector (inc index) (count vector))))

(defn submatrix
  "Takes an m ⨯ n matrix, and returns a new matrix, with dimensions
   (m-1) ⨯ (n-1) with the row and column specified by the indices
   passed in removed."
  [row-idx column-idx matrix]
  (->> matrix
       (mapv (fn [row] (remove-at-index column-idx row)))
       (remove-at-index row-idx)))

(defn- sign
  "Helper function to compute the sign of the multiplier
   for the cell with the given indices for computing cofactors."
  [row-idx column-idx]
  (if (even? (+ row-idx column-idx))
    1
    -1))

; NOTA BENE: There is probably a better way to implement this
; but for now these two declarations are needed because they
; are mutually recursive.
(declare minor)
(declare determinant)

(defn minor
  "Returns the determinant of the submatrix for the given
   row and column indices."
  [row-idx column-idx matrix]
  (let [matrix' (submatrix row-idx column-idx matrix)]
    (determinant matrix')))

(defn cofactor
  "Returns the product of the minor and the sign for the
   row and column indices of the given matrix."
  [row-idx column-idx matrix]
  (* (sign row-idx column-idx)
     (minor row-idx column-idx matrix)))

(defn determinant
  "Returns the determinant of the given matrix."
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

(defn cofactor-matrix
  "Returns another matrix containing the cofactors each
   row and column index of the original matrix. Used in
   computing the determinant of the matrix passed in."
  [matrix]
  (let [dimension (count matrix)]
    (mapv (fn [row-idx]
            (mapv (fn [column-idx]
                    (cofactor row-idx column-idx matrix))
                  (range dimension)))
          (range dimension))))

(defn- inverse*
  "Returns the multiplicative inverse of the given matrix,
   or throws if the matrix has a determinant of zero."
  [matrix]
  (let [d (determinant matrix)]
    (if (zero? d)
      (throw (Exception. "Matrix has no inverse"))
      (-> matrix
          cofactor-matrix
          transpose
          (scalar-divide d)))))

(def inverse (memoize inverse*))
