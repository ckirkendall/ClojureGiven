(ns cljgiven.test.core
  (:use [cljgiven.core])
  (:use [clojure.test]))

(defspec basic-spec 
  (Given [t1 (+ 1 x)
          t2 (- 2 t1)])
  (Context "let us test t1"
           (Given [x (+ 1 3)])
           (When result (+ 1 t1))
           (Then (= 6 result)))
  (Context "let us test t2"
           (Given! [x (+ 1 3)])
           (When result2 (+ t2 x))
           (Then (= 2 result2))))
