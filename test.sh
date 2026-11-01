#!/usr/bin/env bash
# Compile the project with its tests and run every JUnit test.
# Needs the JUnit console jar in the lib folder. The README shows where to get it.
set -e
JAR=$(ls lib/junit-platform-console-standalone-*.jar 2>/dev/null | head -n 1)
if [ -z "$JAR" ]; then
  echo "Walang JUnit jar sa lib folder. Basahin ang README para sa download link."
  exit 1
fi
rm -rf out
mkdir -p out
javac -cp "$JAR" -d out src/tagalog/*.java test/tagalog/*.java
java -jar "$JAR" execute --class-path out --scan-class-path --details=summary --disable-banner
