# Tagalog Script

A small programming language with Tagalog keywords, written in Java. The project includes a lexer, a parser, and an interpreter.

## Example

```
itakda bilang = 1

habang bilang <= 3
    sabihin bilang
    itakda bilang = bilang + 1
tapos
```

Output:

```
1
2
3
```

## Keywords

| Tagalog | Meaning |
|---------|---------|
| itakda | set a variable |
| sabihin | print |
| kung | if |
| kundi | else |
| habang | while |
| tapos | end of block |
| totoo / mali | true / false |
| at / o / hindi | and / or / not |

## How to run

You need Java 17 or newer.

```
javac -d out src/tagalog/*.java
java -cp out tagalog.Main examples/kabuuan.tgl
```

Output:

```
Ang kabuuan ay 55
```

## Options

Print the tokens from the lexer:

```
java -cp out tagalog.Main examples/bilang.tgl --tokens
```

Print the syntax tree from the parser:

```
java -cp out tagalog.Main examples/ekspresyon.tgl --tree
```

Output:

```
Sabihin (+ 1 (* 2 3))
Sabihin (* (grupo (+ 1 2)) 3)
Sabihin (+ (- 5) (/ 10 2))
Sabihin (o (hindi totoo) mali)
Sabihin (at (< 3 5) (>= 5 5))
```

## Errors

Errors appear in Tagalog and show the line number.

```
Error: Linya 2: walang variable na 'bilang'
Error: Linya 2: hindi maaaring hatiin sa zero
Error: Linya 1: inaasahan ang pangalan ng variable pagkatapos ng 'itakda' (nakita: '=')
```

## Project structure

```
src/tagalog/   Lexer, Parser, Interpreter, syntax tree classes, and Main
examples/      Sample .tgl programs
```

## Roadmap

- [x] Lexer
- [x] Parser and syntax tree
- [x] Interpreter for variables and expressions
- [x] Conditions and loops
- [ ] Functions
- [ ] Unit tests with JUnit
