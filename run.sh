#!/bin/bash

rm -r bin/*
./compile.sh
if [ -d bin/p2pclocksync ]; then
	java -cp bin p2pclocksync.agent.Agent "$@"
else
	echo "Compilation error!"
fi
