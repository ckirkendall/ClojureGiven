(ns cljgiven.test.core
  (:use [cljgiven.core])
  (:use [clojure.test]))

(defspec basic-spec 
  (Given [t1 #(+ 1 2)
          t2 #(- 2 2)])
  (Context "let us test t1"
           (When result (t1))
           (Then (= 3 result)))
  (Context "let us test t2"
           (Given [x (+ 1 3)])
           (When result (+ (t2) x))
           (Then (= 1 result))))


