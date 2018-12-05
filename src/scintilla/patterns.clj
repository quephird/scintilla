(ns scintilla.patterns
  (:require [scintilla.matrix :refer [I₄] :as m]))

(defn make-stripe-pattern
  ([color-1 color-2]
   (make-stripe-pattern color-1 color-2 I₄))
  ([color-1 color-2 transform]
   {:pattern-type :stripe
    :transform    transform
    :color-1      color-1
    :color-2      color-2}))

(defmulti color-for
  "Each implementation computes the raw color of the
   pixel specified by the input prepared hit and the
   pattern type."
  (fn [prepared-hit]
    (get-in prepared-hit [:shape :material :pattern :pattern-type])))

(defmethod color-for :stripe
  [{:keys [surface-point] :as prepared-hit}]
  (let [{:keys [color-1 color-2]} (get-in prepared-hit [:shape :material :pattern])
        pattern-transform         (get-in prepared-hit [:shape :material :pattern :transform])
        object-transform          (get-in prepared-hit [:shape :matrix])
        pattern-space-point       (->> surface-point
                                       (m/tuple-times (m/inverse object-transform))
                                       (m/tuple-times (m/inverse pattern-transform)))
        [x _ _ _]                 pattern-space-point
        even-stripe?              (-> x (mod 2.0) int zero?)]
    (if even-stripe? color-1 color-2)))
