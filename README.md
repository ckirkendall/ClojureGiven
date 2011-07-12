# ClojureGiven

Covering ClojureGiven, version 1.0.0-SNAPSHOT.

ClojureGiven is a port of Jim Weirich's rspec-given BDD test framwork to Clojure. 
ClojureGiven is implemented on top of clojure.test through a
set of macros that provide a basic Given/When/Then notation.  



## Status

_ClojureGiven_ is an experimental library and not for production use yet.

## Example

Here is a specification written in the ClojureGiven framework:

<pre>
(ns cljgiven.test.core
  (:use [cljgiven.core])
  (:use [clojure.test]))

(defspec basic-spec 
  (Given [t1 (+ 1 x)
          t2 (- 2 t1)])
  (Context "let us test t1"
           (Given! [x (+ 1 3)])
           (When result (+ 1 t1))
           (Then (= 6 result)))
  (Context "let us test t2"
           (Given! [x (+ 1 3)])
           (When result2 (+ t2 x))
           (Then (= 2 result2))))
</pre>


Below is the output from "lein test"

<pre>
Testing cljgiven.test.core
FAIL in (basic-spec) (core.clj:15)
basic-spec - let us test t2
expected: (= 2 result2)
  actual: (not (= 2 1))
Ran 1 tests containing 2 assertions.
1 failures, 0 errors.
</pre>

Let's talk about the individual statements used in the Given
framework.

### Given

The _Given_ section specifies a starting point, a set of preconditions
that must be true before the code under test is allowed to be run.  In
standard test frameworks the preconditions are established with a
combination of setup methods and code in the test.

In the example code above the preconditions are started with _Given_
statements.  A top level _Given_ (that applies to the entire defspec
block) says that two preconditions exist for a variable t1 & t2 
with some dependent varaible of "_x_".

Note that "_x_" are not specified in the top level defspec
block, but are given in each of the nested contexts.  By pushing the
definition of "_x_" into the nested contexts, we can vary
it as needed for that particular context.

A precondition in the form "(Given [var <expression>])" is lazily evaluated
when the first When of current context or lower context is encountered.  If
you want a non-lazy given, use "(Given! [var <expression>])".

The preconditions are run in order of definition.  Nested contexts
will inherit the preconditions from the enclosing context, with out
preconditions running before inner preconditions.

### When

The _When_ block specifies the code to be tested or specified.  
After the preconditions in the given section are met,
the when code block is run.

There should only be one _When_ block for a given context. However, a
_When_ in an outer context shoud be treated as a _Given_ in an inner
context.  E.g.

<pre>
    (Context "outer context"
      (When code specified in the outer context )
      (Then  assert something about the outer context)

      (Context "inner context"

        ;At this point, the _When_ of the outer context
        ;should be treated as a _Given_ of the inner context

        (When code specified in the inner context)
        (Then assert something about the inner context)))
</pre>

#### When examples:

<pre>
    (When result (inc x))
</pre>

The code block is executed once per test and the value of the code
block is bound to 'result'.  

### Then

The _Then_ sections are the postconditions of the specification. These
then conditions must be true after the code under test (the _When_
block) is run.  

The code in the _Then_ block should be a single 
assertion. Code in _Then_ blocks should not have any side effects.

#### Then examples:

<pre>
    (Then (= 1 result))
</pre>

After the related _When_ block is run, the value of result should be one.  If
it is not one, the test will fail.


