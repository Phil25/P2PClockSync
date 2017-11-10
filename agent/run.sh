#!/bin/bash

rm -r bin/*
./compile.sh
if [ -d bin/main ]; then
	java -cp bin main.Agent "$@"
else
	echo "Compilation error!"
fi
