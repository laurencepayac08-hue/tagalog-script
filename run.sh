#!/usr/bin/env bash
# Compile the project and run a Tagalog program.
# Usage: bash run.sh examples/bilang.tgl [--tokens | --tree]
set -e
mkdir -p out
javac -d out src/tagalog/*.java
java -cp out tagalog.Main "$@"
