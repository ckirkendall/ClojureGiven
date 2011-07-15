(ns cljgiven.core)
(declare process-context)

(def psudo-macros ['Given 'Given! 'When 'Context])

(defn match-psudo [lst sym]
  (and (list? lst) (= (first lst) sym)))

(defn match-all-psudo [lst sym]
  (filter #(match-psudo %1 sym) lst))

(defn no-match-psudo [lst]
  (or (not (list? lst))
      (not-any? #(match-psudo lst %1) psudo-macros)))

(defn process-givens [lst sym] 
  (reduce #(concat %1 (second %2)) [] (match-all-psudo lst sym)))


(defn process-whens [givens lst]
  (let [decon (fn [[sym vr & code]] (list vr (concat `(do) code)))
        whens (reduce #(concat %1 (decon %2))
                      [] (match-all-psudo lst 'When))]
    (if (empty? whens) '()
      (concat givens whens))))


(defn process-all-else ([givens lst] (process-all-else givens lst []))
  ([givens lst accum]
    (if (empty? lst) (seq accum)
      (let [con-func #(match-psudo %1 'Context)
            cur (first lst)
            rst (rest lst)]
        (cond
          (con-func cur) (let [[tmp msg & code1] cur
                               context (process-context givens msg code1)
                               new-accum (conj accum context)]
                           (recur givens rst new-accum))
          (no-match-psudo cur) (recur givens rst (conj accum cur))
          :else (recur givens rst accum))))))
                                          

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

