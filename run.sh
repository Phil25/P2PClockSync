#!/bin/bash

rm -r bin/*
./compile.sh
if [ -d bin/p2pclocksync ]; then
	java -cp bin p2pclocksync.monitor.Monitor "$@"
else
	echo "Compilation error!"
fi
