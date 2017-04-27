#!/bin/bash
rm -r ./bin
mkdir ./bin
cp Input.aa bin/Input.aa
cd src/
jjtree AutoAnalyserParser.jjt        
javacc.sh AutoAnalyserParser.jj
javac *.java
mv *.class ../bin/
cd ../bin/
java AutoAnalyser

