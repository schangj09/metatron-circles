#!/bin/bash

javac --source-path src -d class src/org/baylight/circles/*.java && java -cp class org.baylight.circles.CirclesApp 
