(ns cljgiven.core)
(declare process-context)

(def psudo-macros ['Given 'Given! 'When 'Context])

(defn- match-psudo 
  "identify a psudo macro call"
  [item sym]
  (and (list? item) (= (first item) sym)))

(defn- match-all-psudo 
  "all psudo macro calls identfied of a type in a block of code"
  [lst sym]
  (filter #(match-psudo %1 sym) lst))

(defn- no-match-psudo [item]
  "returns true if the lst is not a psudo macro call"
  (or (not (list? item))
      (not-any? #(match-psudo item %1) psudo-macros)))

(defn- process-givens 
  "used to identify all Give and Given! calls and 
   return a concatnated vector of their contents"
  [lst sym] 
  (reduce #(concat %1 (second %2)) [] (match-all-psudo lst sym)))


(defn- process-whens 
  "used to identify first When call and returns a vector 
   of its contents with the form [when (do ~@code)]"
  [givens lst]
  (let [decon (fn [[sym vr & code]] (list vr (concat `(do) code)))
        whn (first (drop-while #(not (match-psudo %1 'When)) lst))]
    (if (nil? whn) '()
      (concat givens (decon whn)))))


(defn- process-all-else 
  "once givens and whens are processed all other items including
   sub contexts need to be processed."
  ([givens lst] (process-all-else givens lst []))
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
                                          

(defn- process-context 
  "process a sub context"
  [givens message code]
  (let [lz-giv (process-givens code 'Given)
        all-lz-giv (concat lz-giv givens)
        nlz-giv (process-givens code 'Given!)
        whens (process-whens all-lz-giv code)]                        
    `(clojure.test/testing ~message 
          (let ~(vec nlz-giv)
             (let ~(vec whens)
                   ~(conj (process-all-else all-lz-giv code) `do))))))


(defmacro defspec 
  "main macro for defing the spec if deligates to clojure.test/deftest
   and then processes the rest of the content as a sub context"
  [sym & code] 
  `(clojure.test/deftest ~sym ~(process-context '() (str sym " -") code )))


(defmacro Then 
  "this is a alias for the clojure.test/is macro"
  [& code] `(clojure.test/is ~@code))

