#!/bin/bash

javac ./src/p2pclocksync/net/UDPClient.java ./src/p2pclocksync/net/UDPServer.java  ./src/p2pclocksync/data/NetData.java ./src/p2pclocksync/data/ClockData.java ./src/p2pclocksync/agent/Agent.java ./src/p2pclocksync/controller/Controller.java -d ./bin/
