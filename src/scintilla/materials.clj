(ns scintilla.materials)

(defn make-material
  [color ambient diffuse specular shininess]
  {:color     color
   :ambient   ambient
   :diffuse   diffuse
   :specular  specular
   :shininess shininess})

(def default-material
  (make-material [1 1 1] 0.1 0.9 0.9 200))
