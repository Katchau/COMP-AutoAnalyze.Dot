#!/bin/bash
rm -r ./bin
mkdir ./bin
cp Input.aa bin/Input.aa
cp Input2.aa bin/Input2.aa
cp test.dot bin/test.dot
cp ./src/*.java ./bin/
cp ./src/*.jj ./bin
cp ./src/*.jjt ./bin
cd ./bin
jjtree AutoAnalyserParser.jjt
javacc.sh AutoAnalyserParser.jj
javac -cp "*.jar:../libs/*" *.java
find . -name "*.java" -type f -delete
find . -name "*.jj" -type f -delete
find . -name "*.jjt" -type f -delete
java -cp .:"*.jar:../libs/*" AutoAnalyser
#java -cp .:"*.jar:../libs/*" Gui
