# For The Graal

This is a fun absurd interpreter for a mini language.

The parser is implemented in Ruby using a s-expression parser SXP,
the type checker/compiler is written in Java and the interpreter in
JavaScript

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