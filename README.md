# Fuzzer
Java Fuzzer

Dockerfile included.
- Change directory into src "cd src"
- Build Docker file with "docker build -t <Image Name> . "
- Run the docker image with "docker run -it <Image Name>"
- Run the desired test case with JAFL as follows "java JAFL DB_test <test name>" there are 3 different tests.
 > test1.txt - hello (Random String)
 > test2.txt - helloooo (Random String with same length as deadbeef)
 > test3.txt - 1 (Integer)


Extras :
There are four flags in JAFL.java
- printCoverage - prints the coverage percentage.
- printPaths - prints the number of paths explored.
- printTime - prints the execution time.
- printQueueSize - prints the size of the queue.

Set them to true/false to show/hide them.

