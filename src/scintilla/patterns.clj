(ns scintilla.patterns
  (:require [scintilla.color :as c]
            [scintilla.matrix :refer [I₄] :as m]))

(defn make-stripe-pattern
  ([color-1 color-2]
   (make-stripe-pattern color-1 color-2 I₄))
  ([color-1 color-2 transform]
   {:pattern-type :stripe
    :transform    transform
    :color-1      color-1
    :color-2      color-2}))

(defn make-ring-pattern
  ([color-1 color-2]
   (make-ring-pattern color-1 color-2 I₄))
  ([color-1 color-2 transform]
   {:pattern-type :ring
    :transform    transform
    :color-1      color-1
    :color-2      color-2}))

(defn make-checker-pattern
  ([color-1 color-2]
   (make-checker-pattern color-1 color-2 I₄))
  ([color-1 color-2 transform]
   {:pattern-type :checker
    :transform    transform
    :color-1      color-1
    :color-2      color-2}))

(defn make-gradient-pattern
  ([color-1 color-2]
   (make-gradient-pattern color-1 color-2 I₄))
  ([color-1 color-2 transform]
   {:pattern-type :gradient
    :transform    transform
    :color-1      color-1
    :color-2      color-2}))

(def test-pattern
  {:pattern-type :test
   :transform    I₄})

(defmulti color-for
  "Each implementation computes the raw color of the
   pixel specified by the input prepared hit and the
   pattern type."
  (fn [prepared-hit]
    (get-in prepared-hit [:shape :material :pattern :pattern-type])))

(defmethod color-for :test
  [{[x y z _] :surface-point :as prepared-hit}]
  [x y z])

(defmethod color-for :stripe
  [{:keys [surface-point] :as prepared-hit}]
  (let [{:keys [color-1 color-2]} (get-in prepared-hit [:shape :material :pattern])
        pattern-transform         (get-in prepared-hit [:shape :material :pattern :transform])
        object-transform          (get-in prepared-hit [:shape :transform])
        pattern-space-point       (->> surface-point
                                       (m/tuple-times (m/inverse object-transform))
                                       (m/tuple-times (m/inverse pattern-transform)))
        [x _ _ _]                 pattern-space-point
        even-stripe?              (-> x (mod 2.0) Math/floor zero?)]
    (if even-stripe? color-1 color-2)))

(defmethod color-for :ring
  [{:keys [surface-point] :as prepared-hit}]
  (let [{:keys [color-1 color-2]} (get-in prepared-hit [:shape :material :pattern])
        pattern-transform         (get-in prepared-hit [:shape :material :pattern :transform])
        object-transform          (get-in prepared-hit [:shape :transform])
        pattern-space-point       (->> surface-point
                                       (m/tuple-times (m/inverse object-transform))
                                       (m/tuple-times (m/inverse pattern-transform)))
        [x _ z _]                 pattern-space-point
        even-ring?                (-> (+ (* x x) (* z z))
                                      Math/sqrt
                                      (mod 2.0)
                                      Math/floor
                                      zero?)]
    (if even-ring? color-1 color-2)))

(defmethod color-for :checker
  [{:keys [over-point] :as prepared-hit}]
  ;; ACHTUNG: See this post on why we need to use over-point instead of surface-point
  ;;          in order to avoid acne from this pattern:
  ;;
  ;;    http://forum.raytracerchallenge.com/post/34
  (let [{:keys [color-1 color-2]} (get-in prepared-hit [:shape :material :pattern])
        pattern-transform         (get-in prepared-hit [:shape :material :pattern :transform])
        object-transform          (get-in prepared-hit [:shape :transform])
        pattern-space-point       (->> over-point
                                       (m/tuple-times (m/inverse object-transform))
                                       (m/tuple-times (m/inverse pattern-transform)))
        [x y z _]                 pattern-space-point
        even-square?              (-> (+ (Math/floor x) (Math/floor y) (Math/floor z))
                                      (mod 2.0)
                                      zero?)]
    (if even-square? color-1 color-2)))

(defmethod color-for :gradient
  [{:keys [surface-point] :as prepared-hit}]
  (let [{:keys [color-1 color-2]} (get-in prepared-hit [:shape :material :pattern])
        pattern-transform         (get-in prepared-hit [:shape :material :pattern :transform])
        object-transform          (get-in prepared-hit [:shape :transform])
        pattern-space-point       (->> surface-point
                                       (m/tuple-times (m/inverse object-transform))
                                       (m/tuple-times (m/inverse pattern-transform)))
        [x _ _ _]                 pattern-space-point]
    (c/add color-1 (c/scalar-times (c/subtract color-2 color-1) x))))
