(ns cljgiven.core)

(defn process-givens [lst] 
  (reduce #(concat %1 (second %2)) 
          [] (filter #(and (list? %1) (= (first %1) 'Given)) lst)))

(defn process-whens [lst]
  (let [decon (fn [[vr & code]] (list vr (concat `(do) code)))]
    (reduce #(concat %1 (decon (rest %2)))
            [] (filter #(and (list? %1) (= (first %1) 'When)) lst))))

(defn process-all-else [lst]
  (filter #(or (not (list? %1)) 
                    (and (not= (first %1) 'When) 
                         (not= (first %1) 'Given))) lst))


(defmacro defspec [sym & code] 
  `(clojure.test/deftest ~sym  
                         (Context ~(str sym " - ") ~@code )))

(defmacro Context [message & code] 
  (list (symbol "clojure.test/testing") message 
                         (list `let (vec (concat
                                            (process-givens code)
                                            (process-whens code)))
                              (conj (process-all-else code) `do))))

(defmacro Then [& code] `(clojure.test/is ~@code))

