# P2P Clock Synchronization — Project Documentation
#### School project to synchronize clocks accross a peer-to-peer network.
#### author: Filip Tomaszewski
#### group: 12c
#### number: s15403


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
This project contains two applications: Agent and Monitor.

Agent acts as a node on a P2P network. Every Agent except the inital one should contain the address of the initial Agent (passed as 3rd and 4th argument). Upon creation of one, clocks of all Agents are synchronized.

Monitor is used to view status of and manage each Agent, added manually to Monitor's list. Port of the monitor is specified as an arugment, otherwise 3000 is assumed. It can be accessed at `localhost:<port>` with the entirety of its functionality.

The Agents will establish their servers on `localhost` in case the host machine doesn't have internet access, otherwise it will try to find the internal IP of an active interface. The monitor, however, uses an external jQuery library which cannot be accessed without an internet connection.

# Running [^](#contents)
#### Agent

From project root: `agent.[sh/bat] <initial clock> <port> [<init agent hostname> <init agent port>]`
If optional arguments are not specified, the Agent must be the initial Agent.

#### Monitor

From project root: `monitor.[sh/bat] [<port>]`
If optional arguments are not specified, the port 3000 is assumed.

Monitor can be accessed at `localhost:<port>`

# Packages [^](#contents)
## p2pclocksync.agent.Agent [^](#contents)
This is the main class of the Agent application. It takes the following parameters:
* `initial clock` — value of the clock the Agent should begin counting from,
* `port` —  port the Agent should listen to requests on,
* `initial Agent hostname`\* — hostname of the initial Agent to pull data from, and
* `initial Agent port`\* — port of the initial Agent to pull data from.

Agent contains an array of `AgentData`s, which holds appropriate information to contact other Agents.

Furthermore, Agent receives and processes the following messages:
* `CLK` — Send Agent's clock in ms to the requestor.
* `NET` — Build and send a list of all Agents registered to the requestor. The list is built as `addr1:port1;addr2:port2;...;addrN:portN`.
* `SYN` — Download clocks of all registered Agents and set its clock to average of all, including its. Responds with nothing.
* `END` — Shutdown the Agent. Responds with nothing.
* `default` — In case a message is not recognized, it is assumed to contain the address of an Agent. If the address is not on the receiver's list, it is added to it. Otherwise, the address is removed from the list (used when a remote Agent shuts down).

## p2pclocksync.data.AgentData [^](#contents)
This class stores the following information:
* `String ip` — IP of the Agent,
* `int port` — port of the Agent,
* `long clock` — counter in milliseconds,
* `TCPClient client` — instance of the `TCPClient` class to allow communication.

## p2pclocksync.monitor.Monitor [^](#contents)
The Monitor class sets up an HTTP server on `localhost:<port>` (Default port 3000). It is keeping a list of its own of all the Agents. To manage them, it processes GET parameters it reads from the URI, sent by client-side script. `index.html` is the default file that's being sent when the parameters are not recognised, otherwise following occurs for the particular parameters:
* `table=1` — fetches latest clock counts and sents HTML table as a text file,
* `synchronize=X` — sends `SYN` to Agent of the ID `X`,
* `remove=X` — removes the Agent of the ID `X` from the list (doesn't shut it down),
* `end=X` — ends the process of the Agent of the ID `X` by sending `END`,
* `hostname=X&port=Y` — adds Agent of hostname `X` and port `Y`.

## p2pclocksync.monitor.index [^](#contents)
This HTML file contains all the HTML markup, the JavaScript script and CSS styles.

## p2pclocksync.net.ListenThread [^](#contents)
This class is used by `TCPServer` to set up a threaded listener for all accepted sockets.

## p2pclocksync.net.TCPClient [^](#contents)
`TCPClient` provides an abstraction to setting up a communication to and contacting a specific Agent.

## p2pclocksync.net.TCPServer [^](#contents)
`TCPServer` provides an abstraction to receiving requests on the specified port.
