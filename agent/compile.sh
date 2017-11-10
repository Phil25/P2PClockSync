#!/bin/bash

cd src/main/
javac TCPClient.java ListenThread.java TCPServer.java Agent.java -d ../../bin/
cd -
