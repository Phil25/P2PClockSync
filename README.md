# P2P Clock Synchronization — Project Documentation
#### School project to synchronize clocks accross a peer-to-peer network.
#### author: Filip Tomaszewski
#### group: 12c
#### number: s15403


# Contents
* [Overview](#overview-)
* [Running](#running-)
* [Features](#features-)
* [Files](#files-)
	* p2pclocksync.agent
		* [Agent](#agent-)
	* p2pclocksync.data
		* [AgentData](#agentdata-)
	* p2pclocksync.monitor
		* [Monitor](#monitor-)
		* [index](#index-)
	* p2pclocksync.net
		* [ListenThread](#listenthread-)
		* [TCPClient](#tcpclient-)
		* [TCPServer](#tcpserver-)

# Overview [^](#contents)
This project contains two applications: Agent and Monitor.

Agent acts as a node on a P2P network. Every Agent except the inital one should contain the address of the initial Agent (passed as 3rd and 4th argument). Upon creation of one, clocks of all Agents are synchronized.

Monitor is used to view status of and manage each Agent, added manually to Monitor's list. Port of the Monitor is specified as an argument, otherwise 3000 is assumed. It can be accessed at `localhost:<port>` with the entirety of its functionality.

The Agents will establish their servers on `localhost` in case the host machine doesn't have internet access, otherwise it will try to find the internal IP of an active interface. Monitor, however, uses an external jQuery library which cannot be accessed without an internet connection.

# Running [^](#contents)
#### Agent

From project root: `agent.[sh/bat] <initial clock> <port> [<init agent hostname> <init agent port>]`

Example: `./agent.sh 100000 2223 192.168.1.20 2222`

If script not working: `java -cp bin p2pclocksync.agent.Agent <args>`

If optional arguments are not specified, the Agent must be the initial Agent.

#### Monitor

From project root: `monitor.[sh/bat] [<port>]`

Example: `./monitor.sh 3000`

If script not working: `java -cp bin p2pclocksync.monitor.Monitor <args>`

If optional arguments are not specified, the port 3000 is assumed.

Monitor can be accessed at `localhost:<port>`

# Features [^](#contents)
What has or hasn't been implemented, plus potential bugs.
### Agent:
* (+) Each Agent sets up a TCP server on a given port.
* (+) Every Agent keeps the list of the structure of the network, updated  dynamically.
* (+) Agents' clocks are synchronized every time an Agent leaves or joins the network.
* (+) Simple command processor. Type "exit" or "quit" to close the Agent.
* (+) Agent captures shutdown event to broadcast its shutdown and a `SYN` message.
* (-) Shutdown of a remote Agent can be detected, but it is done this way to preserve the abstraction of `TCPServer`.
* (-) Clock synchronization doesn't account for latency.
* (-) No security measures, Agents accept connection or messages from anything.
### Monitor:
* (+) Sets up an HTTP server on a given port.
* (+) Provides a dynamically updated table of Agents.
* (+) The table consists of Agents' IPs, ports and options.
* (+) Options constitue of sending `SYN` or `END` messages, and removing the Agent from the list.
* (+) Agents are added manually to the list using input fields under the table.
* (+) If connection to Agent could not be made or was lost, a reconnection attempt is made every 3 seconds.
* (+) The above assures complete independence between Agents and the Monitor.
* (+) A client-side script calls the server to build the list and downloads it.
* (-) Table is built by the server on each call, there is no limit to how often the calls are made.
* (-) `index.html` file is parsed each time the webpage is opened, instead of being cached.
* (-) HTML markup, CSS style and JavaScript script are all kept in the same file.
* (-) No security measures, Monitor manages Agents by processing GET variables.
* (?) The same Agent could be added more than once.

# Files [^](#contents)
## Agent [^](#contents)
This is the main class of the Agent application. It takes the following parameters:
* `initial clock` — value of the clock the Agent should begin counting from,
* `port` —  port the Agent should listen to requests on,
* `initial Agent hostname`\* — hostname of the initial Agent to pull data from, and
* `initial Agent port`\* — port of the initial Agent to pull data from.

Agent contains an array of `AgentData`s, which holds appropriate information to contact other Agents.

Furthermore, Agent receives and processes the following messages:
* `CLK` — Send Agent's clock in milliseconds to the requestor.
* `NET` — Build and send a list of all Agents registered to the requestor. The list is built as `addr1:port1;addr2:port2;...;addrN:portN`.
* `SYN` — Download clocks of all registered Agents and set its clock to average of all, including its. Responds with nothing.
* `END` — Shutdown the Agent. Responds with nothing.
* `default` — In case a message is not recognized, it is assumed to contain the port of an Agent. The full address is then constructed by getting the Socket's remote host address. If the address is not on the receiver's list, it is added to it. Otherwise, the address is removed from the list (used when a remote Agent shuts down).

## AgentData [^](#contents)
This class stores the following information:
* `String ip` — IP of the Agent,
* `int port` — port of the Agent,
* `long clock` — counter in milliseconds,
* `TCPClient client` — instance of the `TCPClient` class to allow communication.

## Monitor [^](#contents)
The Monitor class sets up an HTTP server on `localhost:<port>` (default port 3000). It is keeping a list of its own of all the Agents. To manage them, it processes GET parameters it reads from the URI, sent by client-side script. `index.html` is the default file that's being sent when the parameters are not recognised, otherwise following occurs for the particular parameters:
* `table=1` — fetches latest clock counts and sents HTML table as a text file,
* `synchronize=X` — sends `SYN` to Agent of the ID `X`,
* `remove=X` — removes the Agent of the ID `X` from the list (doesn't shut it down),
* `end=X` — ends the process of the Agent of the ID `X` by sending `END`,
* `hostname=X&port=Y` — adds Agent of hostname `X` and port `Y`.

## index [^](#contents)
This HTML file contains all the HTML markup, the JavaScript script and CSS styles.

## ListenThread [^](#contents)
This class is used by `TCPServer` to set up a threaded listener for all accepted sockets.

## TCPClient [^](#contents)
`TCPClient` provides an abstraction to setting up a communication to and contacting a specific Agent.

## TCPServer [^](#contents)
`TCPServer` provides an abstraction to receiving requests on the specified port.
