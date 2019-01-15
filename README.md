## Scintilla

### Project goals

* Use familiar PL first, namely Clojure
* Make as mutation free and purely functional as possibly
* Use of raw data structures instead of Java objects or Clojure `defrecord`s
* Implement everything, no external libraries
* Focuses on learning concepts, making code as readable as possible, not caring about performance, lots of docstrings and comments

### Usage

#### Shapes 

* Sphere
* Cube
* Plane
* Cylinder
* Cone

List of shared shape attributes

* `:material`
* `:transform`

List of attributes for cones and cylinders

* `:minimum`
* `:maximum`
* `:capped?`

```
(make-sphere)
```


#### Materials

List of material attributes supported:

* `:ambient`
* `:color`
* `:diffuse`
* `:pattern`
* `:reflective`
* `:refractive-index`
* `:shininess`
* `:specular`
* `:transparency`

```
(make-material)

(make-sphere {:material material})
```

#### Transforms 

* Scaling
* Translation
* Rotation on x,y, and z axes
* Shearing


```
(make-transform)

(make-sphere {:transform transform})
```

#### Patterns

List of pattern types

List of pattern attributes

```
(make-pattern)

(make-material {:pattern pattern})

(make-sphere {:material material})
```


#### Groups

```
(make-group [...] transform)
```

#### Light

```
(make-light ...)
```

#### Scene

#### Camera

#### Rendering

### Future goals

Use libraries with support for BLAS or GPUs for improved performance
Take a drive with a differnt PL

### Important links

*


### License
Copyright (C) 2019, ⅅ₳ℕⅈⅇℒℒⅇ Ҝⅇℱℱoℜⅆ.

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.