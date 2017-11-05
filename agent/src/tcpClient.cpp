

#include <iostream>
#include <string.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>

#include "../include/tcpClient.h"

//#define DEBUG

tcpClient::tcpClient(std::string ip, int port) : ip(ip), port(port), connected(false){
	sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if(sock < 0){
		std::cerr << "Error creating socket. (" << sock << ")" << std::endl;
		return;
	}

#ifdef DEBUG
	std::cout << "Socket created." << std::endl;
#endif
	if(port == 0)
		return;

	struct sockaddr_in serv;
	bzero((char*)&serv, sizeof(serv));
	serv.sin_family			= AF_INET;
	serv.sin_addr.s_addr	= inet_addr(ip.c_str());
	serv.sin_port			= htons(port);

#ifdef DEBUG
	std::cout << "Connecting to " << ip << ":" << port << "..." << std::endl;
#endif
	if(connect(sock, (sockaddr *)&serv, sizeof(serv)) >= 0)
		connected = true;

#ifdef DEBUG
	std::cout << "Connection " << (connected ? "" : "not ") << "established." << std::endl;
#endif
}

tcpClient::~tcpClient(){
	close(sock);
}

bool tcpClient::send(std::string data){
	if(!connected){
		std::cerr << "Connection has not been established." << std::endl;
		return false;
	}

	size_t len = data.size();
	if(len < 1){
		std::cerr << "Message is empty." << std::endl;
		return false;
	}

	return ::send(sock, data.c_str(), len, 0) >= 0;
}

std::string tcpClient::recv(size_t size){
	unsigned char buffer[size];
	int bytes = ::recv(sock, buffer, size, 0);
	if(bytes < 1)
		return "";
	std::string resp(&buffer[0], &buffer[0] +bytes);
	return resp;
}
