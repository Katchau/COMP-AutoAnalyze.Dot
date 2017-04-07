#!/bin/bash
jjtree AutoAnalyserParser.jjt        
javacc AutoAnalyserParser.jj
javac *.java
java AutoAnalyser
