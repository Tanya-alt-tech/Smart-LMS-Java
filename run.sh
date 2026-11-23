#!/bin/sh
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -d out -sourcepath src src/lms/Main.java
java -cp out lms.Main
