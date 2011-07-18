(ns cljgiven.test.core
  (:use [cljgiven.core])
  (:use [clojure.test]))

;simple test of basic structure

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
           (Then (= 2 result2)))) ;this test is designed to fail


;testing the sub context and the 
;proper handling of of overriding
;stack by a When
(defspec stack 
  (Given [stack init-obj])
  (Context "testing a vector as a stack"
           (Given [init-obj [1 3]])
           (When stack (conj stack 2)) ; push 2 on the stack
           (Then (= 2 (peek stack)))
           (Then (= [1 3 2] stack))
           (Context "testing pop on a vecor"
                    (When stack (pop stack))
                    (Then (= [1 3] stack))))
  (Context "testing a list as a stack"
           (Given [init-obj '(1 3)])
           (When stack (conj stack 2)) ; push 2 on the stack
           (Then (= 2 (peek stack)))
           (Then (= '( 2 1 3) stack))
           (Context "testing pop on a list"
                    (When stack (pop stack))
                    (Then (= '(1 3) stack)))))


