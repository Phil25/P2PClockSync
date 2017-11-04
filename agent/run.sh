#!/bin/bash

rm bin/agent
./compile.sh
./bin/agent "$@"
