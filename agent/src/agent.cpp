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

void sleep(unsigned int);
int getCommunicateId(std::string);
int getAgentOfAddress(std::string, int);
void addAddress(std::string);
void addAddress(std::string, int);

void counter(){
	while(ms < 1000){
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
			std::size_t pos = 0, found;
			while((found = data.find_first_of(';', pos)) != std::string::npos){
				arr.push_back(data.substr(pos, found -pos));
				pos = found +1;
			}
			arr.push_back(data.substr(pos));
			for(unsigned long i = 0; i < arr.size(); i++)
				addAddress(arr[i]);
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

	ms = atoi(argv[1]);
	std::string ip = "127.0.0.1";
	int port = atoi(argv[2]);
	std::ostringstream os;
	os << ip << ":" << port;
	address = os.str();
	os.clear();

	std::thread thr_counter(counter);
	std::cout << "Counter thread initialized: " << thr_counter.get_id() << std::endl;
	thr_counter.detach();

	if(initPort != 0){
		addAddress(initIp, initPort);
		tcpClient client(initIp, initPort);
		client.send("NET");
		processResponse(initIp, initPort, client.recv(), NET);
		client.send(address);
	}

	tcpServer server(ip, port, processRequest);
	return 0;
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

void addAddress(std::string data){
	std::cout << "Adding address: " << data << std::endl;
	int delimPos = data.find(":");
	agents.push_back(agentData{
		data.substr(0, delimPos), // IP
		stoi(data.substr(delimPos +1, data.length() -1)), // Port
		0 // Clock
	});
}

void addAddress(std::string ip, int port){
	std::cout << "Adding address: " << ip << ":" << port << std::endl;
	agents.push_back(agentData{ip, port, 0});
}
