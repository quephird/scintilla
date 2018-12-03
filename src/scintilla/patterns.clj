(ns scintilla.patterns)

(defn make-stripe-pattern
  [color-1 color-2]
  {:pattern-type :stripe
   :color-1      color-1
   :color-2      color-2})

(defmulti color-for
  "Each implementation computes the raw color of the
   pixel specified by the input prepared hit and the
   pattern type."
  (fn [prepared-hit]
    (get-in prepared-hit [:shape :material :pattern :pattern-type])))

(defmethod color-for :stripe
  [{:keys [surface-point] :as prepared-hit}]
  (let [{:keys [color-1 color-2]} (get-in prepared-hit [:shape :material :pattern])
        {:keys [surface-point]}   prepared-hit
        [x _ _ _]                 surface-point
        even-stripe?             (-> x (mod 2.0) int zero?)]
    (if even-stripe? color-1 color-2)))
