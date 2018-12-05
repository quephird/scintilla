(ns scintilla.materials)

;; TODO:: Improve this interface using named kwargs
(defn make-material
  [color ambient diffuse specular shininess pattern]
  {:color     color
   :ambient   ambient
   :diffuse   diffuse
   :specular  specular
   :shininess shininess
   :pattern   pattern})

(def default-material
  (make-material [1 1 1] 0.1 0.9 0.9 200 nil))

(defn set-color
  [material color]
  (assoc material :color color))

(defn set-diffuse
  [material diffuse]
  (assoc material :diffuse diffuse))

(defn set-specular
  [material specular]
  (assoc material :specular specular))

(defn set-pattern
  [material pattern]
  (assoc material :pattern pattern))
  
