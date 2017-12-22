# P2P Clock Synchronization — Project Documentation (**UDP** variant)
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
		* [MessageProcessor](#messageprocessor-)
	* p2pclocksync.data
		* [AgentData](#agentdata-)
	* p2pclocksync.controller
		* [Controller](#conroller-)
	* p2pclocksync.net
		* [UDPServer](#udpclient-)
		* [UDPServer](#udpserver-)

# Overview [^](#contents)

This project contains two applications: Agent and Controller.

Agent acts as a node on a P2P network. Every Agent should know the broadcast address of the network beforehand, in order to communicate with other Agents.

Controller is used to manage values of each Agent, for example change its synchronization period. The address of the Agent to be controlled is specified as the first argument, and optionally, first command after that. Controller will remember the address and initialize a chat session with the user, where the user can pass any arguments to be sent to the targeted Agent.

# Running [^](#contents)

#### Agent

From project root: `agent.[sh/bat] <initial counter> <period> <broadcast address>`

Example: `./agent.sh 10000 1 192.168.1.255`

If script not working: `java -cp bin p2pclocksync.agent.Agent <args>`

Initial counter is given in milliseconds, period is given in seconds.

#### Controller

From project root: `controller.[sh/bat] [<address> <arg1> <arg2> ...]`

Example: `./controller.sh 192.168.1.20 set period 5`

If script not working: `java -cp bin p2pclocksync.controller.Controller <args>`

If optional arguments are not specified, it can be inputed to the running process, along with any command afterwards.

# Features [^](#contents)
What has or hasn't been implemented, plus potential bugs.

### Agent:

* (+) Each Agent sets up a UDP server on port 22222.
* (+) Agents' counters are synchronized periodically, as specified.
* (+) Agent waits for an arbitrary number of responses and calculates its counter after a timeout of the same length as the period.
* (+) Counter synchronization accounts for latency by calculating time difference using local time.
* (-) Period change occurs after the next consecutive synchronization, this could be a long time in case the previous value is large.
* (-) No security measures, Agents execute commands from any source, with no delay.

### Controller:

* (+) Arguments are translated to a command and sent to the specified address.
* (+) Response is properly received and displayed.
* (+) Command processor runs in a loop in the application.
* (-) Receive buffer might get stuffed in case a broadcast address is given. //TODO: check this

# Files [^](#contents)

## Agent [^](#contents)

This is the main class of the Agent application. It takes the following parameters:

* `initial counter` — counter in milliseconds the Agent should begin counting from,
* `period` —  period in seconds every which the Agent should synchronize its counter with others,
* `broadcast address` — broadcast address of the network other Agents are set up on.

Furthermore, Agent receives and processes the following messages:

* `get counter` — returns Agent's counter.
* `get period` — returns Agent's period.
* `set counter X` — sets Agent's counter to _X_.
* `set period X` — sets Agent's period to _X_.

## MessageProcessor [^](#contents)

`MessageProcessor` seperates the Agent and processing incoming messages. It contains an object of Agent which allows it to return any data necessary.

## CounterData [^](#contents)

Provides an abstraction of returning corrected time by adding the passed time from when the counter was saved.

Example: `CounterData cd = new CounterData(1000); sleep(2000); print cd.getTime()` — prints 3000.

## Controller [^](#contents)

The Controller class is used to provide an interface for interacting with specific Agents on the network, addressing them by their address as provided by the user. Implements a simple command processor for continuous interaction.

It takes the following parameters:

* `address`\* — the address of the agent.
* `cmds...`\* — the first command to be executed, not necessairly given in quotation marks.

If no arguments are specified the Controller will prompt the user to input the address.

Any input will be sent to the Agent and response will be returned, provided the Agent can process it correctly (commands are as specified in the Agent's class description).

## UDPClient [^](#contents)

`UDPClient` provides an abstraction for sending and broadcasting data accross the network.

## UDPServer [^](#contents)

`UDPServer` provides an abstraction for receiving requests on the specified port.
