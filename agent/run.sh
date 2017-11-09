#!/bin/bash

rm -r bin/*
./compile.sh
if [ -d bin/main ]; then
	java -cp bin main.Agent 0 2222
else
	echo "Compilation error!"
fi
