# ClojureGiven

Covering ClojureGiven, version 1.0.0-SNAPSHOT.

ClojureGiven is a port of Jim Weirich's rspe-given BDD test framwork to Clojure. 
ClojureGiven is implemented on top of clojure.test through a
set of macros that provide a basic Given/When/Then notation.  



## Status

_ClojureGiven_ is an experimental library and not for production use yet.

## Example

Here is a specification written in the ClojureGive framework:

<pre>
(ns cljgiven.test.core
  (:use [cljgiven.core])
  (:use [clojure.test]))

(defspec basic-spec 
  (Given [t1 (+ 1 x)
          t2 (- 2 t1)])
  (Context "let us test t1"
           (Given! [x (+ 1 3)])
           (When result (+ 1 t1))
           (Then (= 6 result)))
  (Context "let us test t2"
           (Given! [x (+ 1 3)])
           (When result2 (+ t2 x))
           (Then (= 2 result2))))
</pre>

