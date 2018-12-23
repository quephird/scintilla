(ns scintilla.example
  (:require [scintilla.camera :as c]
            [scintilla.lighting :as l]
            [scintilla.materials :as a]
            [scintilla.matrix :as m]
            [scintilla.numeric :refer :all]
            [scintilla.patterns :as p]
            [scintilla.rendering :as r]
            [scintilla.scene :as e]
            [scintilla.shapes :as s]
            [scintilla.transformation :as t]))

(defn sphere-with-light
  []
  (let [transform      (t/scaling-matrix 0.5 0.5 0.5)
        material       (a/make-material {:ambient 0.1
                                         :color [1 0.2 1]
                                         :diffuse 0.9
                                         :shininess 20
                                         :specular 0.9
                                         :pattern nil})
        sphere         (s/make-sphere material transform)
        light          (l/make-light [-10 10 -10 1] [1 1 1])
        scene          (e/make-scene [sphere] light)
        view-transform (t/view-transform-matrix-for [0 0 -1 1]
                                                    [0 0 0 1]
                                                    [0 1 0 0])
        camera         (c/make-camera 100 100 π⟋2 view-transform)]
    (r/render-to-file camera scene "sphere-with-light.ppm")))

(defn- make-floor
  []
  (let [material  (-> a/default-material
                      (a/set-color [1 0.9 0.9])
                      (a/set-specular 0.0))
        transform (t/scaling-matrix 10 0.01 10)]
    (s/make-sphere material transform)))

(defn- make-left-wall
  []
  (let [material  (-> a/default-material
                      (a/set-color [1 0.9 0.9])
                      (a/set-specular 0.0))
        transform (->> (t/scaling-matrix 10 0.01 10)
                       (m/matrix-times (t/rotation-x-matrix π⟋2))
                       (m/matrix-times (t/rotation-y-matrix (- π⟋4)))
                       (m/matrix-times (t/translation-matrix 0 0 5)))]
    (s/make-sphere material transform)))

(defn- make-right-wall
  []
  (let [material  (-> a/default-material
                      (a/set-color [1 0.9 0.9])
                      (a/set-specular 0.0))
        transform (->> (t/scaling-matrix 10 0.01 10)
                       (m/matrix-times (t/rotation-x-matrix π⟋2))
                       (m/matrix-times (t/rotation-y-matrix π⟋4))
                       (m/matrix-times (t/translation-matrix 0 0 5)))]
    (s/make-sphere material transform)))

(defn- make-left-sphere
  []
  (let [material  (-> a/default-material
                      (a/set-color [0.9 0.3 0.2])
                      (a/set-diffuse 0.7)
                      (a/set-specular 0.3))
        transform (-> (t/translation-matrix -1.5 0.33 -0.75)
                      (m/matrix-times (t/scaling-matrix 0.3 0.3 0.3)))]
    (s/make-sphere material transform)))

(defn- make-middle-sphere
  []
  (let [material  (-> a/default-material
                      (a/set-color [0.4 0.4 0.8])
                      (a/set-diffuse 0.7)
                      (a/set-specular 0.3))
        transform (t/translation-matrix -0.5 1 0.5)]
    (s/make-sphere material transform)))

(defn- make-right-sphere
  []
  (let [material  (-> a/default-material
                      (a/set-color [0 0.5 0.5])
                      (a/set-diffuse 0.7)
                      (a/set-specular 0.3))
        transform (-> (t/translation-matrix 1.5 0.5 -0.5)
                      (m/matrix-times (t/scaling-matrix 0.5 0.5 0.5)))]
    (s/make-sphere material transform)))

(defn three-spheres-in-corner
  []
  (let [floor          (make-floor)
        left-wall      (make-left-wall)
        right-wall     (make-right-wall)
        left-sphere    (make-left-sphere)
        middle-sphere  (make-middle-sphere)
        right-sphere   (make-right-sphere)
        scene          (e/make-scene [left-sphere middle-sphere right-sphere floor left-wall right-wall] l/default-light)
        view-transform (t/view-transform-matrix-for [0 1.5 -5 1]
                                                    [0 1 0 1]
                                                    [0 1 0 0])
        camera         (c/make-camera 400 200 π⟋3 view-transform)]
    (r/render-to-file camera scene "three-spheres-in-corner.ppm")))

(defn three-spheres-on-plane
  []
  (let [floor-material (-> a/default-material
                           (a/set-color [1 0.9 0.9])
                           (a/set-specular 0.0))
        floor          (s/make-plane floor-material)
        left-sphere    (make-left-sphere)
        middle-sphere  (make-middle-sphere)
        right-sphere   (make-right-sphere)
        scene          (e/make-scene [left-sphere middle-sphere right-sphere floor] l/default-light)
        view-transform (t/view-transform-matrix-for [0 1.5 -5 1]
                                                    [0 1 0 1]
                                                    [0 1 0 0])
        camera         (c/make-camera 400 200 π⟋3 view-transform)]
    (r/render-to-file camera scene "three-spheres-on-plane.ppm")))

(defn- make-sphere-with-stripes
  []
  (let [stripes   (p/make-stripe-pattern [1 0 0.5] [1 1 0])
        material  (-> a/default-material
                      (a/set-color nil)
                      (a/set-diffuse 0.7)
                      (a/set-specular 0.3)
                      (a/set-pattern stripes))
        transform (t/translation-matrix -0.5 1 0.5)]
    (s/make-sphere material transform)))

(defn sphere-with-stripes-on-plane
  []
  (let [floor-material (-> a/default-material
                           (a/set-color [1 0.9 0.9])
                           (a/set-specular 0.0))
        floor          (s/make-plane floor-material)
        sphere         (make-sphere-with-stripes)
        scene          (e/make-scene [sphere floor] l/default-light)
        view-transform (t/view-transform-matrix-for [0 1.5 -5 1]
                                                    [0 1 0 1]
                                                    [0 1 0 0])
        camera         (c/make-camera 400 200 π⟋3 view-transform)]
    (r/render-to-file camera scene "sphere-with-stripes-on-plane.ppm")))

(defn three-patterned-spheres-on-plane
  []
  (let [transform      (t/rotation-y-matrix π⟋6)
        stripes        (p/make-stripe-pattern [1 1 1] [0.7 0.7 0.7] transform)
        floor-material (-> a/default-material
                           (a/set-pattern stripes))
        floor          (s/make-plane floor-material)

        p-transform-1  (m/matrix-times
                         (t/scaling-matrix 0.1 0.1 0.1)
                         (t/rotation-y-matrix (- π⟋3)))
        stripes-1      (p/make-stripe-pattern [0.5 0 1] [1 1 0] p-transform-1)
        material-1     (-> a/default-material
                            (a/set-pattern stripes-1))
        o-transform-1  (t/translation-matrix -2.25 1 1.0)
        sphere-1       (s/make-sphere material-1 o-transform-1)

        p-transform-2  (t/rotation-y-matrix (+ π⟋6))
        gradient-2     (p/make-gradient-pattern [1 0.5 0] [0 0 1] p-transform-2)
        material-2     (-> a/default-material
                            (a/set-pattern gradient-2))
        o-transform-2  (t/translation-matrix 0 1 1.5)
        sphere-2       (s/make-sphere material-2 o-transform-2)

        p-transform-3  (->> (t/scaling-matrix 0.5 0.5 0.5)
                            (m/matrix-times (t/rotation-x-matrix π⟋2)))
        checker-3      (p/make-checker-pattern [1 0 0] [0.2 1 0.2] p-transform-3)
        material-3     (-> a/default-material
                            (a/set-pattern checker-3))
        o-transform-3  (t/translation-matrix 2.25 1 2.0)
        sphere-3       (s/make-sphere material-3 o-transform-3)

        scene          (e/make-scene [sphere-1 sphere-2 sphere-3 floor] l/default-light)

        view-transform (t/view-transform-matrix-for [0 1.5 -5 1]
                                                    [0 1 0 1]
                                                    [0 1 0 0])
        camera         (c/make-camera 400 200 π⟋3 view-transform)]
    (r/render-to-file camera scene "three-patterned-spheres-on-plane.ppm")))

(defn glassy-spheres-on-plane
  []
  (let [checkers       (p/make-checker-pattern [1 1 1] [0 0 0])
        floor-material (a/make-material {:color   nil
                                         :pattern checkers})
        floor          (s/make-plane floor-material)

        glass          (a/make-material {:ambient          0.1
                                         :diffuse          0.1
                                         :refective        0.1
                                         :refractive-index 1.52
                                         :specular         0.9
                                         :transparency     1.0})
        transform1      (t/translation-matrix -1.5 1 0.5)
        glassy-sphere1  (s/make-sphere (a/set-color glass [0.3 0.1 0.6]) transform1)

        transform2      (t/translation-matrix 1.5 1 0.5)
        glassy-sphere2  (s/make-sphere (a/set-color glass [0.6 0.3 0.1]) transform2)

        scene          (e/make-scene [glassy-sphere1 glassy-sphere2 floor] l/default-light)
        view-transform (t/view-transform-matrix-for [0 1.5 -5 1]
                                                    [0 1 0 1]
                                                    [0 1 0 0])
        camera         (c/make-camera 400 200 π⟋3 view-transform)]
    (r/render-to-file camera scene "glassy-spheres-on-plane.ppm")))

(defn cube-with-light
  []
  (let [checkers       (p/make-checker-pattern [1 0.5 0] [0.5 0 1])
        transform      (m/matrix-times
                        (t/rotation-x-matrix (- π⟋6))
                        (t/rotation-y-matrix π⟋4))
        material       (a/make-material {:ambient 0.1
                                         :color nil
                                         :diffuse 0.9
                                         :shininess 20
                                         :specular 0.9
                                         :pattern checkers})
        cube           (s/make-cube material transform)
        light          (l/make-light [-10 10 -10 1] [1 1 1])
        scene          (e/make-scene [cube] light)
        view-transform (t/view-transform-matrix-for [0 0 -5 1]
                                                    [0 0 0 1]
                                                    [0 1 0 0])
        camera         (c/make-camera 400 400 π⟋3 view-transform)]
    (r/render-to-file camera scene "cube-with-light.ppm")))
