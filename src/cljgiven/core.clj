(ns cljgiven.core)
(declare process-context)

(def psudo-macros ['Given 'Given! 'When 'Context])

(defn- match-psudo 
  "identify a psudo macro call"
  [item sym]
  (and (list? item) (= (first item) sym)))

(defn- match-all-psudo 
  "all psudo macro calls identified of a type in a block of code"
  [lst sym]
  (filter #(match-psudo %1 sym) lst))

(defn- no-match-psudo [item]
  "returns true if the item is not a psudo macro call"
  (or (not (list? item))
      (not-any? #(match-psudo item %1) psudo-macros)))

(defn- process-givens 
  "used to identify all Give and Given! calls and 
   return a concatenated vector of their contents"
  [lst sym] 
  (reduce #(concat %1 (second %2)) [] (match-all-psudo lst sym)))


(defn- process-when 
  "used to identify first When call and returns a vector 
   of its contents with the form [var (do ~@code)]"
  [givens lst]
  (let [decon (fn [[sym vr & code]] `(~vr (do ~@code)))
        whn (first (drop-while #(not (match-psudo %1 'When)) lst))]
    ;don't process givens if there is no when statement
    (if (nil? whn) '() 
      (decon whn))))


(defn- override-lazy-givens 
  "removes the lazy givens that have been overriden by either
   Give! or When"
  [givens non-lazy-givens whn] 
  (let [syms (set (filter #(symbol? %1) (concat non-lazy-givens whn)))]
    (loop [lst givens  accum '()]
      (if (empty? lst) accum
        (let [vr (first lst)
              code (second lst)]
          (if (contains? syms vr) (recur (rest (rest lst)) accum)
            (recur (rest (rest lst)) (concat accum (list vr code)))))))))
           

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
          ;if its sub context process it
          (con-func cur) (let [[tmp msg & code1] cur
                               context (process-context givens msg code1)
                               new-accum (conj accum context)]
                           (recur givens rst new-accum))
          
          ;if its not func call and its not something we already
          ;processed just add it to the code block
          (no-match-psudo cur) (recur givens rst (conj accum cur)) 
          
          ;if is something we already processed skip it
          :else (recur givens rst accum))))))
                                          

(defn- process-context 
  "process a sub context"
  [givens message code]
  (let [lz-giv (process-givens code 'Given)   ;vector lazy givens in this context
        all-lz-giv (concat lz-giv givens)     ;lazy givens this context and parent
        nlz-giv (process-givens code 'Given!) ;all non-lazy given this context
        whn (process-when all-lz-giv code)    ;when statement from this context  
        wg (if (empty? whn) '() (concat all-lz-giv whn))  ;bundle the givens and when together
        new-givs (override-lazy-givens all-lz-giv nlz-giv whn)] ;remove the overriden givens for futur contexts                   
    `(clojure.test/testing ~message 
          (let ~(vec nlz-giv)
             (let ~(vec wg)
                   ~(conj (process-all-else new-givs code) `do))))))


(defmacro defspec 
  "main macro for defing the spec if deligates to clojure.test/deftest
   and then processes the rest of the content as a sub context"
  [sym & code] 
  `(clojure.test/deftest ~sym ~(process-context '() (str sym " -") code )))


(defmacro Then 
  "this is a alias for the clojure.test/is macro"
  [& code] `(clojure.test/is ~@code))

