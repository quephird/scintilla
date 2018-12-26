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

;; TODO: Get rid of these; they're not useful anymore
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
  
