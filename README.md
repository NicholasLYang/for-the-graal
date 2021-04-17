# For The Graal

This is a fun absurd interpreter for a mini language.

The parser is implemented in Ruby using a s-expression parser SXP,
the type checker/compiler is written in Java and the interpreter in
JavaScript

## Language

A program consists of a list of statements. Currently, there are functions,
 if statements, let statements and print statements.

The language supports numbers (double precision floating point), strings and bools.

Functions are defined using `fun`. They take in a name, a list of params,
and a body expression. Params are a name/type pair.
```
(
  (fun foo ((n number) (b number)) (* n b))
)
```

Let statements are defined using `let`. They take in a name and an expression.

```
(
  (let a (* 25 (/ 30 10)))
)
```

Print statements are defined using `print`. They take in a single expression.
```
(
  (print "foobar")
)
```

If statements are defined using `if`. They take in a condition, and 
a then block with an optional else block.
```
(
)
```
## Running
Before running, you should have Graal with Ruby, JavaScript and Java
all installed. Also, please make sure to run `bundle install` 
to install `sxp`.

Compiling is simply:
```
javac -Xlint:unchecked -classpath src/ src/Main.java
```
and running:
```
java -classpath src/ Main <source_file>
```

Where `<source_file>` is a file containing the source code. 
Check out `examples/` for, well, examples.

`javac` and `java` must be the Graal versions, of course.

NOTE: You must run the code from the root directory as it loads files
via relative paths.