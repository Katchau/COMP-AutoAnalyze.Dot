#!/bin/bash
rm -r ./bin
mkdir ./bin
cp Input.aa bin/Input.aa
cp ./src/*.java ./bin/
cp ./src/*.jj ./bin
cp ./src/*.jjt ./bin
cd ./bin
jjtree AutoAnalyserParser.jjt        
javacc.sh AutoAnalyserParser.jj
javac *.java
find . -name "*.java" -type f -delete
find . -name "*.jj" -type f -delete
find . -name "*.jjt" -type f -delete
java AutoAnalyser


