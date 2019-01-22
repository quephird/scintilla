## Scintilla

### Purpose

This is a fully featured ray tracer written in Clojure, based on the wonderful book by Jamis Buck, [The Ray Tracer Challenge](https://pragprog.com/book/jbtracer/the-ray-tracer-challenge). This is still a work in progress, so keep watching this space!

### Project goals

I've been wanting to write a ray tracer for _years_ but every single time I looked for any resources on writing one, they were always either too advanced or based on C or C++... and I am a _lousy_ C programmer. So, when I got an email from PragProg announcing the newly available beta release of a book on ray tracing, I instantaneously clicked on the link. And after thumbing through the table of contents and a sample chapter, it was clear that this the first book actually targeting people like me that have not yet built a system as complex s this. 

The book only supplies tests which you need to get to pass, and only pseudocode or code fragments as implementations; the rest is up to you. I chose Clojure, since it's the programming language that I'm most comfortable with and in the event I needed libraries, I had the JVM and the whole Java ecosystem at my disposal.

However, it turned out that the book actually shows you how to build _everything_ from the ground up, from being able perform matrix and vector arithmetic to generating an image file in `.ppm` format. And so I decided that these would be my goals for this project:

* Make the code mutation free and purely functional as possible
* Use raw data structures instead of Java objects or Clojure `defrecord`s
* Implement everything and not bring in any external libraries
* Focus on making the code as readable as possible, not caring about performance
* Putting in as many docstrings and comments as possible
* Keep things as simple as possible.

Regarding simplicity, most of the "objects" in this project are either Clojure vectors or merely hashmaps, with keywords as keys. Restricting myself to data types native to Clojure made it much simpler to experiment and evolve the codebase. I must admit that there were times that I _did_ wish I had types that the compiler could have checked rather than simply getting cryptic errors or unexpected results at runtime.

### Usage

# TODO: Elaborate more here
It's necessary to describe the components of this implementation from the bottom up. You should see a pattern appear throughout most of the code here; options for all of the object constructors are supplied as simple hashmaps with keyword keys.

#### Vectors, points, rays, and matrices

Vectors and points are merely Clojure vectors of length 4, with the fourth component differentiating the two types. `[1 2 3 1]` is the point (1, 2, 3) and `[1 2 3 0]` is the vector 𝒊 + 2𝒋 + 3𝒌.

Rays are hashmaps with keys `:point` and `:direction`, and can be made with the `scintilla.ray/make-ray`. To make a ray that starts at the origin and points to (-1, 3, -5), just call:

```clj
(require '[scintilla.ray :as r])

(r/make-ray [0 0 0 1] [-1 3 -5 0])
```

Matrices (2-dimensional ones, that is) are implemented merely as a vector of vectors. There aren't any convenience methods for creating matrices, but all of the common matrix manipulation functions are in `scintilla.matrix`. So, if you wanted to take the determinant of a matrix, you could do the following:

```clj
(require '[scintilla.matrix :as m])

(def some-matrix [[1 2 3 4] [2 3 4 5] [3 4 5 6] [4 5 6 7]])

(m/determinant some-matrix)
```

# TODO: Fill this out
#### Colors

#### Shapes

Shapes are all defined in `scintilla.shapes`; there are five different primitive shapes implemented in this ray tracer:

* Sphere
* Cube
* Plane
* Cylinder
* Cone

For example, you can create a new sphere, set at the origin with all other default attributes, by simply typing:

```clj
(require '[scintilla.shapes :as s])

(s/make-sphere)
```

By default, _all_ shapes are centered at the origin, and different shape types can have different properties and default values for them.

#TODO: fix this
The table below lists them as well as their default attributes:

| Shape | Defaults |
|---|---|
| Sphere | radius: 1 |
| Cube | length: 2 |
| Plane | lies in xz-plane |
| Cylinder | radius: 1 <br> main axis: y <br> minimum height: -∞ <br> maximum height: ∞ <br> capped?: false |
| Cone | main axis: y <br> minimum height: -∞ <br> maximum height: ∞ <br> capped?: false |



Cones and cylinders are slightly more complicated and have these additional properties:

* `:minimum` - the y value of correspondent with the bottom of the shape
* `:maximum` - the y value of correspondent with the top of the shape
* `:capped?` - set to `true` if the ends are closed or `false` if open

Currently there are two attributes that are shared across all shapes:

* `:material` - the hashmap that describes various optical properties of the shape
* `:transform` - the matrix that describes how a shape is moved or altered

We shall talk about these in greater detail below.

#### Materials

Materials are use to describe the all of the optical properties of a specific shape. The following attributes are currently supported:

* `:ambient` - a value from 0.0 to 1.0 which controls the proportion of the object's color used in its shadowed portions. The default value is 0.1.
* `:color` - the actual color of the shape if a pattern is not specified. The default value is `[1 1 1]`.
* `:diffuse` - a value from 0.0 to 1.0 which controls how matte-like the surface of the shape should be. The default value is 0.9.
* `:pattern` - the pattern associated with the shape whose implementation computes the color. There is no default pattern and instead the color value is used if no pattern is specified.
* `:reflective`
* `:refractive-index` - a value greater than or equal to 1.0 which effectively controls how a light ray bends when it moves from one medium into another. The default value is 1.0.
* `:shininess` -  a unbounded positive value that controls the size of the specular highlight on a shape. The default value is 200.
* `:specular` - a value from 0.0 to 1.0 which controls the proportion of light from a light source that reflects off of a point on the surface of the shape should contribute to the overall color. The default value is 0.9.
* `:transparency`

It should be noted that this ray tracer employs a form of the Phong reflection algorithm for modelling how light reflects off surfaces, and so several of thes properties correspond with concepts in that model. You can read more about this algorithm in this excellent [Wikipedia page](https://en.wikipedia.org/wiki/Phong_reflection_model).

As an example, to create a purple sphere, you must first create a material with that color, and then associate that material with the sphere, as in the following: 

```clj
(require '[scintilla.materials :as a])
(require '[scintilla.shapes :as s])

(def purple-material (a/make-material {:color [0.5 0.0 1.0]}))

(s/make-sphere {:material material})
```

Unspecified options for a material will be set to their default values as listed above.

#### Transforms 

There are four different classes of transformations that can be applied to three dimensional shapes; they are the following:

* Scaling
* Translation
* Rotation on x,y, and z axes
* Shearing

Each of these types can be mapped to a set of 4x4 matrices; you can read more about these so-called affine transformations [here](https://www.mathworks.com/help/images/matrix-representation-of-geometric-transformations.html#bvnhvau).

```
(require '[scintilla.materials :as a])
(require '[scintilla.shapes :as s])

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
* 3D affine transformation matrices  
  [https://www.mathworks.com/help/images/matrix-representation-of-geometric-transformations.html#bvnhvau](https://www.mathworks.com/help/images/matrix-representation-of-geometric-transformations.html#bvnhvau)
* Wikipedia page on the Phong reflection model  
  [https://en.wikipedia.org/wiki/Phong_reflection_model](https://en.wikipedia.org/wiki/Phong_reflection_model)  
* 

### License
Copyright (C) 2019, ⅅ₳ℕⅈⅇℒℒⅇ Ҝⅇℱℱoℜⅆ.

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.