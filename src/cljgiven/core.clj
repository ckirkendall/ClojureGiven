(ns cljgiven.core)

(defmacro defspec [sym & code] `(clojure.test/deftest ~sym ~@code ))

(defmacro Given [& code] `(let ~@code))

(defmacro When [sym & code] 
  (let [execList (filter #(and (list? %1)
                               (not= (first %1) 'Then) 
                               (not= (first %1) 'Context)) code)
        thenList (filter #(or (= (first %1) 'Then) 
                              (= (first %1) 'Context)) code)]
  (concat (list `let [sym (conj execList `do)]) thenList)))

(defmacro Context [message & code] `(clojure.test/testing ~message ~@code))

(defmacro Then [& code] `(clojure.test/is ~@code))
