#!/bin/bash

cd src/
g++ -pthread -std=c++11 -o agent `ls`
cd ..
mv src/agent bin/agent
