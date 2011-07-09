(ns cljgiven.test.core
  (:use [cljgiven.core])
  (:use [clojure.test]))

(defspec basic-spec 
  (Given [t1 #(+ 1 2)
          t2 #(- 2 2)]
         (Context "let us test t1"
                  (When result (t1)
                        (Then (= 3 result))))
         (Context "let us test t2"
                  (When result (t2)
                        (Then (= 0 result))))))
