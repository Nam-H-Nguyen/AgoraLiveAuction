#!/usr/bin/env bash
find ./src -name "*.java" | xargs javac
java -cp ./src client.ClientServlet $1
