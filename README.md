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
## p2pclocksync.agent.Agent [^](#contents)
This is the main class of the Agent application. It takes the following parameters:
* `initial clock` — value of the clock the Agent should begin counting from,
* `port` —  port the Agent should listen to requests on,
* `initial agent hostname`\* — hostname of the initial agent to pull data from, and
* `initial agent port`\* — port of the initial agent to pull data from.

Agent contains an array of `AgentData`s, which holds appropriate information to contact other agents.

## p2pclocksync.data.AgentData [^](#contents)
This class stores the following information:
* `String ip` — hostname of the agent,
* `int port` — port of the agent,
* `long clock` — counter in milliseconds,
* `TCPClient client` —  TCP client to allow communication.

## p2pclocksync.monitor.Monitor [^](#contents)
The Monitor class sets up an HTTP server on `localhost:3000`. It is keeping a list of its own of all the agents. To manage them, it processes GET parameters it reads from the URI, sent by client-size script. `index.html` is the default file that's being sent when the parameters are not recognised, otherwise following occurs for the particular parameters:
* `table=1` — fetches latest clock counts and sents HTML table as a text file,
* `synchronize=X` — Sends SYN to Agent of the ID `X`,
* `remove=X` — Removes the Agent of the ID `X`,
* `end=X` — Ends the process of the Agent of the ID `X`,
* `hostname=X&port=Y` — Adds Agent of hostname `X` and port `Y`.

## p2pclocksync.monitor.index [^](#contents)
This HTML file contains all the HTML markup, JavaScript and CSS styles.

## p2pclocksync.net.ListenThread [^](#contents)
This class is used by `TCPServer` to set up a threaded listener for all incoming sockets.

## p2pclocksync.net.TCPClient [^](#contents)
`TCPClient` provides an abstraction to setting up a communication to and contacting a specific Agent.

## p2pclocksync.net.TCPServer [^](#contents)
`TCPServer` provides an abstraction to receiving requests on a specified port.
