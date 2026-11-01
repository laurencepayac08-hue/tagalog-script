# Tagalog Script

A small programming language with Tagalog keywords, written in Java. The project includes a lexer, a parser, an interpreter, functions, and a JUnit test suite.

## Example

```
gawin factorial(n)
    kung n <= 1
        ibalik 1
    tapos
    ibalik n * factorial(n - 1)
tapos

sabihin factorial(5)
```

Output:

```
120
```

## Keywords

| Tagalog | Meaning |
|---------|---------|
| itakda | set a variable |
| sabihin | print |
| kung / kundi | if / else |
| habang | while |
| tapos | end of block |
| gawin | define a function |
| ibalik | return a value |
| totoo / mali | true / false |
| at / o / hindi | and / or / not |

Numbers, text in quotation marks, and comments that start with `#` are also supported. Operators: `+ - * / == != < <= > >=`.

## Functions

```
gawin dagdag(a, b)
    ibalik a + b
tapos

sabihin dagdag(2, 3)
```

- A function sees global variables and its own variables.
- `itakda` inside a function creates a local variable. The global variable with the same name stays unchanged.
- A function without `ibalik` gives back `wala`.
- Recursion works. Very deep recursion stops with an error message.

## How to run

You need Java 17 or newer.

```
bash run.sh examples/function.tgl
```

Or compile and run the commands yourself:

```
javac -d out src/tagalog/*.java
java -cp out tagalog.Main examples/function.tgl
```

Print the tokens from the lexer, or the syntax tree from the parser:

```
java -cp out tagalog.Main examples/bilang.tgl --tokens
java -cp out tagalog.Main examples/ekspresyon.tgl --tree
```

## Errors

Errors appear in Tagalog and show the line number.

```
Error: Linya 2: walang variable na 'bilang'
Error: Linya 2: hindi maaaring hatiin sa zero
Error: Linya 4: inaasahan ang 2 argumento pero 1 ang ibinigay
Error: Linya 1: inaasahan ang pangalan ng variable pagkatapos ng 'itakda' (nakita: '=')
```

## Tests

The project has 61 JUnit tests for the lexer, the parser, and the interpreter.

1. Download the JUnit console jar from [Maven Central](https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar).
2. Put the jar in the `lib` folder.
3. Run the tests.

```
bash test.sh
```

## Project structure

```
src/tagalog/    Lexer, Parser, Interpreter, syntax tree classes, and Main
test/tagalog/   JUnit tests
examples/       Sample .tgl programs
lib/            Place for the JUnit jar (not uploaded to GitHub)
run.sh          Compile and run a program
test.sh         Compile and run all tests
```

## Roadmap

- [x] Lexer
- [x] Parser and syntax tree
- [x] Interpreter for variables and expressions
- [x] Conditions and loops
- [x] Functions
- [x] Unit tests with JUnit
- [ ] Remainder operator
- [ ] Lists
