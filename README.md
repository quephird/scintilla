## Scintilla

### Purpose

This is a fully featured ray tracer written in Clojure, based on the wonderful book by Jamis Buck, [The Ray Tracer Challenge](https://pragprog.com/book/jbtracer/the-ray-tracer-challenge). This is still a work in progress, so keep watching this space!

### Project goals

I've been wanting to write a ray tracer for _years_ but every single time I looked for any resources on writing one, they were always either too advanced or based on C or C++... and I am a _lousy_ C programmer. So, when I got an email from PragProg announcing the newly available beta release of a book on ray tracing, I instantaneously clicked on the link. And after thumbing through the table of contents and a sample chapter, it was clear that this the first book actually targeting people like me that have not yet built a system as complex s this. 

The book only supplies tests which you need to get to pass, and only pseudocode or code fragments as implementations; the rest is up to you. I chose Clojure, since it's the programming language that I'm most comfortable with and in the event I needed libraries, I had the JVM and the whole Java ecosystem at my disposal.

However, it turned out that the book actually makes you build _everything_ from the ground up, from code to perform matrix and vector arithmetic to being able to generate a file in the PPM format. And so I decided that these would be my goals for this project:

* Make the code mutation free and purely functional as possible
* Use raw data structures instead of Java objects or Clojure `defrecord`s
* Implement everything and not bring in any external libraries
* Focus on making the code as readable as possible, not caring about performance
* Putting in as many docstrings and comments as possible

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

* The Ray Tracer Challenge listing on the PragProg site.  
  [https://pragprog.com/book/jbtracer/the-ray-tracer-challenge](https://pragprog.com/book/jbtracer/the-ray-tracer-challenge)  
* Wikipedia page on the Phong reflection model  
  [https://en.wikipedia.org/wiki/Phong_reflection_model](https://en.wikipedia.org/wiki/Phong_reflection_model)  
* 

### License
Copyright (C) 2019, ⅅ₳ℕⅈⅇℒℒⅇ Ҝⅇℱℱoℜⅆ.

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.