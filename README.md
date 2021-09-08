# 3apl-interpreter
An implementation of interpreter for 3APL (an agent-oriented programming language),
language specification available at: http://www.cs.uu.nl/3apl/
# Overview
+ This project is developed as my [dissertation](docs/dissertation.pdf) for bachelor's degree at University of Nottingham.
+ This interpreter comes with a GUI which supports creating, running and interacting with multi-agent systems.
+ It also supports customizing graphical agent environments.
# Development/Runtime Environment
+ JDK11 with JavaFx module installed.
# Getting Started
1. Compile and package this project into a executable `jar` file, `TripleAPL_interpreter` is the main class.
2. Run `java -jar 3apl.jar`
 
+ For loading 3APL programs, read [user manual](docs/UserManual.pdf).
+ For writing 3APL programs, check the example codes available in `/src/TripleAPL/`, read [dissertation](docs/dissertation.pdf) and language specification.
# Credits
+ The parser is developed using JavaCC.
+ tuProlog is used for Prolog inference.