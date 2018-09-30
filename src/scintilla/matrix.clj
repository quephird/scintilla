(ns scintilla.matrix)

(defn transpose
  [m]
  (let [column-count (count (first m))]
    (mapv (fn [column-idx]
            (mapv (fn [row]
                    (get row column-idx)) m)) (range column-count))))

(defn matrix-times
  [m1 m2]
  (mapv (fn [row1]
          (mapv (fn [col1]
                  (reduce + (map * row1 col1)))
                (transpose m2)))
        m1))
