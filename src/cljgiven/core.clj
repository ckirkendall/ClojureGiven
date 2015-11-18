(ns cljgiven.core)
(declare get-syms)
(declare suround-let)
(declare set-re-exec)

;Map for looking up lazy accessor varables
;Each test and creats a local binding for this 
(def ^:dynamic *clj-given* (atom {:global :holder}))

;#######################################################
; Primary Macros Given,Given!,When, Then
;#######################################################
(defmacro defspec  [sym & body] 
  `(clojure.test/deftest ~sym 
                         (binding [*clj-given* (atom {})]
                           ~@body)))

(defmacro Given [vect] 
  "Creates a lazy accessor for variables" 
  (seq (reduce #(conj %1 (conj %2 'set-given-var)) ['do] 
          (partition 2 vect))))

(defmacro Given! [vect]
  "Similar to Given but does not provide lazy eval"  
  (seq (reduce #(conj %1 (conj %2 'set-given-var!)) ['do] 
          (partition 2 vect))))

(defmacro When [sym & body] 
  "Allows the output of this block to be assigned to a 
   symbol that can be used in your Then clause"
  `(set-given-var! ~sym (do ~@body)))

(defmacro Then [body]
  "alias for the clojure.test macro 'is' that also
   allows for access to Given, Given! and When vars"
  `(eval (suround-let (quote (clojure.test/is ~body)))))    
  
(defmacro Context [msg & body]
  "alias for clojure.text 'testing' also creats the 
   local binding for Given vars"
  `(clojure.test/testing ~msg 
            (binding [*clj-given* (atom @*clj-given*)]
              ~@body)))


;#######################################################
; Helper functions and Macros 
;#######################################################

(defmacro get-given-var [sym]
  "Helper macro that allows access to the given vars
  fist checks to see if variable already has been 
  evaluated and only evaluates if required"
  `(let [var-map# @*clj-given*
         sym-map# ((quote ~sym) var-map#) 
	       val-exists# (:val-exists sym-map#)
	       re-exec# (:re-exec sym-map#)] 
     (if (or re-exec# (not val-exists#)) 
       (do 
         (let [new-val# (eval (suround-let (:func sym-map#)))
               t1-sym-map# (assoc sym-map# :val new-val#)
               t2-sym-map# (assoc t1-sym-map# :val-exists true)
              new-sym-map# (assoc t2-sym-map# :re-exec false)]
           (reset! *clj-given* (assoc var-map# (quote ~sym) new-sym-map#))
           new-val#))
       (:val sym-map#))))
  
(defmacro set-given-var [sym gval]
  "Helper macro that registers a lazy accessor Given
   in the context binding"
  `(let [sym-map# {:val-exists false, 
                   :func (quote (do ~gval)), 
                   :re-exec true, 
                   :type :given
                   :syms (quote ~(set (get-syms gval)))}] 
     (set-re-exec (quote ~sym))
     (reset! *clj-given* (assoc @*clj-given* (quote ~sym) sym-map#))))


(defmacro set-given-var! [sym gval]
  "Helper macro that evaluates and sets the Given! vars
   in the context binding"
  `(let [sym-map# {:val-exists true, 
                   :func nil, 
                   :val (eval (suround-let (quote ~gval))), 
                   :re-exec false, 
                   :type :given!}]
     (set-re-exec (quote ~sym))
     (reset! *clj-given* (assoc @*clj-given* (quote ~sym) sym-map#))))


(defn get-syms [ls] 
  "get list of all the symbols for a block of code
   used to determin if a lazy accessor should be 
   re-evaluated"
  (if (not ls) []
    (let [sym-func (fn [lst item] 
                     (cond
                       (and (symbol? item) (not-any? #(= item %1) lst)) (conj lst item)
                       (coll? item) (reduce  #(if (contains? (set %1) %2) %1 
                                                (conj %1 %2)) lst  
                                             (get-syms item))
                       :else lst))]
      (cond
        (coll? ls) (reduce sym-func [] ls)
        (symbol? ls) '(ls)
        :else '()))))

  
(defn suround-let [& body]
  "Helper function that surounds a block of 
   code with let containing variables refrenced
   in the block of code that have been registered
   as Given, Given! or When blocks"
  (let [syms (get-syms body)
        var-vec (reduce 
                  #(if (contains? @*clj-given* %2) 
                     (concat %1 [%2 (list 'cljgiven.core/get-given-var %2)])
                     %1) [] syms)]
    `(let ~(vec var-vec)
       ~@body)))


(defn set-re-exec [sym]
  "helper function that sets a lazy accessor back
   to being re-evaluated. This is done anytime a variable
   that is refrenced in its code block is overriden by
   either a Given! or When"
  (let [var-map @*clj-given*
        kys (keys var-map)
        fkys (filter #(contains? (:syms (%1 var-map)) sym) kys)]
    (doseq [ky fkys]
      (let [mp (assoc (var-map ky) :re-exec true)]
       (reset! *clj-given* (assoc @*clj-given* ky mp))))))




