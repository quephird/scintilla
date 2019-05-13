(ns scintilla.materials)

(def default-material
  {:ambient          0.1
   :color            [1 1 1]
   :diffuse          0.9
   :pattern          nil
   :reflective       0.0
   :refractive-index 1.0
   :shininess        200
   :specular         0.9
   :transparency     0.0})

(defn make-material
  [overrides]
  (merge default-material overrides))
