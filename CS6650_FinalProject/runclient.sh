<<<<<<< HEAD
#!/usr/bin/env bash
find ./src -name "*.java" | xargs javac
java -cp ./src client.ClientServlet $1
=======
find ./src -name "*.java" | xargs javac
java -cp ./src client.ClientServlet 32000
>>>>>>> d887371a31b523c9144872d7fae6d41e05db8093
