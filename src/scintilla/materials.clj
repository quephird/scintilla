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

(defn set-color
  [material color]
  (assoc material :color color))

(defn set-diffuse
  [material diffuse]
  (assoc material :diffuse diffuse))

(defn set-specular
  [material specular]
  (assoc material :specular specular))
