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

## Project structure

```
src/tagalog/   Java source files
examples/      Sample .tgl programs
```

## How to run

```
javac -d out src/tagalog/*.java
java -cp out tagalog.Main examples/bilang.tgl
```

## Roadmap

- [x] Lexer
- [ ] Parser and syntax tree
- [ ] Interpreter for variables and expressions
- [ ] Conditions and loops
- [ ] Functions
- [ ] Error messages in Tagalog
- [ ] Unit tests with JUnit
