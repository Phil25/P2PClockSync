#include <iostream>
#include <chrono>
#include <thread>
#include <vector>
#include <sstream>

#include "../include/tcpServer.h"
#include "../include/tcpClient.h"

#define CLK 0 // clock
#define NET 1 // request agents' ips and ports
#define SYN 2 // call for syncronization
#define ADR 3 // address to be added

struct agentData{
	std::string ip;
	int port;
	unsigned int clock;
};

unsigned int ms = 0;
std::string address = "", initIp = "";
int initPort = 0;
std::vector<agentData> agents;

void processUserCommand(std::string);
void sleep(unsigned int);
int getCommunicateId(std::string);
int getAgentOfAddress(std::string, int);
void split(const std::string&, char, std::vector<std::string>&);
void addAddress(std::string);
void addAddress(std::string, int);

void counter(){
	while(1){
		sleep(1);
		ms++;
	}
}

std::string processRequest(std::string data){
	int id = getCommunicateId(data);
	switch(id){
		case CLK:
			return std::to_string(ms);

		case NET:{
			if(agents.size() < 1)
				return ":(";
			std::ostringstream os;
			unsigned long len = agents.size();
			for(unsigned long i = 0; i < len; i++)
				os << agents[i].ip << ":" << agents[i].port << (i == len -1 ? "" : ";");
			return os.str();
		}

		case ADR:
			addAddress(data);
			break;
	}
	return "";
}

void processResponse(std::string ip, int port, std::string data, int type){
	switch(type){
		case CLK:{
			unsigned int otherClock = stoi(data);
			if(otherClock == 0)
				break;
			int agent = getAgentOfAddress(ip, port);
			if(agent != -1)
				agents[agent].clock = otherClock;
			break;
		}

		case NET:{
			if(data.size() < 7)
				break;
			std::vector<std::string> arr;
			split(data, ';', arr);
			for(unsigned long i = 0; i < arr.size(); i++){
				addAddress(arr[i]);
				int last = agents.size() -1;
				tcpClient client(agents[last].ip, agents[last].port);
				client.send(address);
			}
		}
	}
}

int main(int argc, char *argv[]){
	if(argc < 3){
		std::cerr << "Plase provide counter value and port number as arguments." << std::endl;
		return 1;
	}

	if(argc > 4){
		initIp = argv[3];
		initPort = atoi(argv[4]);
	}

	// Assign general stuff
	ms = atoi(argv[1]);
	std::string ip = "127.0.0.1";
	int port = atoi(argv[2]);
	std::ostringstream os;
	os << ip << ":" << port;
	address = os.str();
	os.clear();

	// Create counter thread
	std::thread thr_counter(counter);
	std::cout << "Counter thread initialized: " << thr_counter.get_id() << std::endl;
	thr_counter.detach();

	// Initialize early agent interaction
	if(initPort != 0){
		addAddress(initIp, initPort);
		tcpClient client(initIp, initPort);
		client.send("NET");
		processResponse(initIp, initPort, client.recv(), NET);
		client.send(address);
	}

	// Set up TCP listen server
	tcpServer server(ip, port, processRequest);

	// Handle user commands
	while(1){
		std::string cmd;
		std::getline(std::cin, cmd);
		if(cmd == "exit" || cmd == "quit")
			break;
		processUserCommand(cmd);
	}

	// Broadcast disconnection to other agents
	for(unsigned long i = 0; i < agents.size(); i++){
		tcpClient client(agents[i].ip, agents[i].port);
		client.send(address);
	}

	return 0;
}

void processUserCommand(std::string cmd){
	std::ostringstream os;
	if(cmd == "clock" || cmd == "timer" || cmd == "ms")
		os << "Current clock: " << ms << "ms" << std::endl;

	else if(cmd == "list"){
		auto end = agents.end();
		for(auto it = agents.begin(); it != end; it++)
			os << it->ip << ":" << it->port << " -- " << it->clock << "ms" << std::endl;
		if(os.str().length() == 0)
			os << "No other agents registered." << std::endl;
	}

	else if(cmd == "addr" || cmd == "address")
		os << "Current address: " << address << std::endl;

	if(os.str().size() == 0)
		std::cout << "Unknown command." << std::endl;
	else
		std::cout << os.str();
}

void sleep(unsigned int x){
	std::this_thread::sleep_for(std::chrono::milliseconds(x));
}

int getCommunicateId(std::string data){
	if(data == "CLK")
		return CLK;
	if(data == "NET")
		return NET;
	if(data.find(":") != std::string::npos)
		return ADR;
	return SYN;
}

int getAgentOfAddress(std::string ip, int port){
	for(unsigned long i = 0; i < agents.size(); i++)
		if(agents[i].ip == ip && agents[i].port == port)
			return i;
	return -1;
}

void split(const std::string &data, char delimiter, std::vector<std::string> &arr){
	std::size_t pos = 0, found;
	while((found = data.find_first_of(delimiter, pos)) != std::string::npos){
		arr.push_back(data.substr(pos, found -pos));
		pos = found +1;
	}
	arr.push_back(data.substr(pos));
}

void addAddress(std::string data){
	int delimPos = data.find(":");
	std::string addIp = data.substr(0, delimPos);
	int addPort = stoi(data.substr(delimPos +1, data.length() -1));
	addAddress(addIp, addPort);
}

void addAddress(std::string ip, int port){
	int id = getAgentOfAddress(ip, port);
	if(id == -1)
		agents.push_back(agentData{ip, port, 0});
	else
		agents.erase(agents.begin() +id);
	std::cout << (id == -1 ? "Added" : "Removed") << " address: " << ip << ":" << port << std::endl;
}
