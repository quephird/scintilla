(ns scintilla.materials)

(defn make-material
  [{:keys [ambient color diffuse pattern reflective shininess specular] :as overrides}]
  {:ambient    ambient
   :color      color
   :diffuse    diffuse
   :pattern    pattern
   :reflective reflective
   :shininess  shininess
   :specular   specular})

(def default-material
  {:ambient 0.1
   :color [1 1 1]
   :diffuse 0.9
   :pattern nil
   :reflective 0.0
   :shininess 200
   :specular 0.9})

(defn set-ambient
  [material ambient]
  (assoc material :ambient ambient))

(defn set-color
  [material color]
  (assoc material :color color))

(defn set-diffuse
  [material diffuse]
  (assoc material :diffuse diffuse))

(defn set-pattern
  [material pattern]
  (assoc material :pattern pattern))

(defn set-relective
  [material reflective]
  (assoc material :reflective reflective))

(defn set-shininess
  [material shininess]
  (assoc material :shininess shininess))

(defn set-specular
  [material specular]
  (assoc material :specular specular))
  
