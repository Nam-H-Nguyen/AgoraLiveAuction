#!/usr/bin/env bash
find ./src -name "*.java" | xargs javac
java -cp ./src test.RunSimulation $1
