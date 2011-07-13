(ns cljgiven.core)
(declare process-context)

(defn process-givens [lst sym] 
  (reduce #(concat %1 (second %2)) 
          [] (filter #(and (list? %1) (= (first %1) sym)) lst)))


(defn process-whens [givens lst]
  (let [decon (fn [[vr & code]] (list vr (concat `(do) code)))
        whens (reduce #(concat %1 (decon (rest %2)))
                      [] (filter #(and (list? %1) (= (first %1) 'When)) lst))]
    (if (empty? whens) '()
      (concat givens whens))))


(defn process-all-else ([givens lst] (process-all-else givens lst []))
  ([givens lst accum]
    (if (empty? lst) (seq accum)
      (let [cur (first lst)]
        (cond
          (and (list? cur) 
               (= (first cur) 'Context)) (let [rst (rest lst)
                                               msg (second cur)
                                               code1 (rest (rest cur))
                                               context (process-context givens msg code1)
                                               new-accum (conj accum context)]
                                           (process-all-else givens rst new-accum))
          (or (not (list? cur))
              (and (list? cur)
                   (not= (first cur) 'When)
                   (not= (first cur) 'Given!)
                   (not= (first cur) 'Given))) (process-all-else givens (rest lst) (conj accum cur))
          :else (process-all-else givens (rest lst) accum))))))
                                          

(defn process-context [givens message code]
  (let [lz-giv (process-givens code 'Given)
        all-lz-giv (concat lz-giv givens)
        nlz-giv (process-givens code 'Given!)
        whens (process-whens all-lz-giv code)]                        
    `(clojure.test/testing ~message 
          (let ~(vec nlz-giv)
             (let ~(vec whens)
                   ~(conj (process-all-else all-lz-giv code) `do))))))


(defmacro defspec [sym & code] 
  `(clojure.test/deftest ~sym ~(process-context '() (str sym " -") code )))



(defmacro Then [& code] `(clojure.test/is ~@code))

