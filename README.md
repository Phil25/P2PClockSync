# P2P Clock Synchronization — Project Documentation
#### School project to synchronize clocks accross a peer-to-peer network.


# Contents
* [Overview](#overview-)
* [Running](#running-)
* [Packages](#packages-)
	* p2pclocksync.agent
		* [Agent](#p2pclocksync.agent.Agent-)
	* p2pclocksync.data
		* [AgentData](#p2pclocksync.data.AgentData-)
	* p2pclocksync.monitor
		* [Monitor](#p2pclocksync.monitor.Monitor-)
		* [index](#p2pclocksync.monitor.index-)
	* p2pclocksync.net
		* [ListenThread](#p2pclocksync.net.ListenThread-)
		* [TCPClient](#p2pclocksync.net.TCPClient-)
		* [TCPServer](#p2pclocksync.net.TCPServer-)

# Overview [^](#contents)
This project contains two applications: Agent and Monitor. Agent acts as a node on a P2P network. Every Agent except the inital one should contain address of the initial Agent (passed as 3rd and 4th argument). Upon creation of one, clocks of all Agents are synchronized. Monitor is used to view status of and manage each Agent, added manually to Monitor's list. It is accessed at `localhost:3000`.

# Running [^](#contents)
#### Agent

From project root: `agent.sh <initial clock> <port> [<init agent hostname> <init agent port>]`

#### Monitor

From project root: `monitor.sh`

Monitor is accessed at `localhost:3000`

# Packages [^](#contents)
### p2pclocksync.agent.Agent [^](#contents)
This is the main class of the Agent application. It takes the following parameters:
* `initial clock` — value of the clock the Agent should begin counting from,
* `port` —  port the Agent should listen to requests on,
* `initial agent hostname`\* — hostname of the initial agent to pull data from, and
* `initial agent port`\* — port of the initial agent to pull data from.

<!--
* [Agent](#agent-)
	* [AgentData](#agentdata-)
	* [TCPClient](#tcpclient-)
	* [TCPServer](#tcpserver-)
	* [ListenThread](#listenthread-)
	* [Example interaction](#example-interaction-)


# Agent
`Agent` is the node of the network. It contains the class `AgentData`, which holds the appropriate information. To contact other agents, it uses custom classes called `TCPClient` and `TCPServer`.

## AgentData
Agent's own data is stored in an instance of `AgentData` in variable `thisData`. Data of other agents is kept in an instance of `ArrayList<AgentData>` in variable `data`.
The subclass stores the following information:
* `String ip` — hostname of the agent,
* `int port` — port of the agent,
* `long clock` — counter in miliseconds,
* `TCPClient client` — `TCPClient` to allow communication.

## TCPClient
This class provides a mean open a connection and send data to a specific agent via `Socket`. Connection is kept open.

`public TCPClient(String hostname, int port)` — constructor

`public String send(String msg)` — send `msg` and return the response

`public void close()` — overrides `close()` method from Closeable interface

## TCPServer
This class provides a mean to listen on certain port for TCP connections via `SocketServer`. The listening and accepting is done on a seperate thread. Moreover, every incoming connection gets assigned a new thread, and managed using the class `ListenThread`.

`public TCPServer(int port, Function<String, String> func)` — constructor, `func` takes the incoming message as parameter and returns a potential response

`public void detach()` — seize accepting connections

`public String toString()` — overrides `toString()` method by displaying the address the socket listens on.

## ListenThread
This class manages seperate clients connected to `ServerSocket` from the `TCPServer` class.

`public ListenThread(Socket socket, Function<String, String> func)` — constructor, listen to client on `socket` and pass incoming data to `func` as argument; send potential response back to the client

`public void run()` — overrides `run()` method from Thread, allows the listening to run concurrently

## Example interaction
*Situation*: network has two agents: A and B. Agent A is the introducing agent. Agent C joins the network.
* Agent C downloads from agent A a list of contacts.
* Agent C sends its address to agents on the downloaded list.
* Agent A and B update their contact list with agent C.
* Agent C, using CLK, downloads from agents A and B their clocks.
* Agent C updates its clock with the average of downloaded ones.
* Agent C sends SYN to agents A and B.
* Both agents A and B download clocks from other agents and update theirs.-->
