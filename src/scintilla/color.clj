(ns scintilla.color
  (:refer-clojure :exclude [+ - * /]))

(defprotocol Color
  (+ [v1 v2])
  (- [v1 v2])
  (* [v s])
  (◦ [v1 v2]))

(extend-type clojure.lang.PersistentVector
  Color
  (+ [v1 v2]
    (mapv clojure.core/+ v1 v2))
  (- [v1 v2]
    (mapv clojure.core/- v1 v2))
  (* [v s]
    (mapv #(clojure.core/* s %) v))
  (◦ [v1 v2]
    (mapv clojure.core/* v1 v2)))
