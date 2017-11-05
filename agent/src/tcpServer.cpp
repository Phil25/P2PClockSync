#include <string.h>
#include <thread>
#include <iostream>
#include <arpa/inet.h>
#include <errno.h>
#include <unistd.h>

#include "../include/tcpServer.h"

//#define DEBUG

tcpServer::tcpServer(std::string ip, int port, std::string (*callback_ptr)(std::string)):
callback_ptr(callback_ptr),
running(true){
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if(sockfd == -1){
		std::cerr << "Could not create socket." << std::endl;
		return;
	}
	bzero((char*)&addr, sizeof(addr));

	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(ip.c_str());
	addr.sin_port = htons(port);

	int bindErr = bind(sockfd, (struct sockaddr *)&addr, sizeof(addr));
	if(bindErr == -1){
		std::cerr << "Could not bind socket on " << inet_ntoa(addr.sin_addr) << ":" << ntohs(addr.sin_port) << ": " << errno << " (" << strerror(errno) << ")" << std::endl;
		return;
	}

	if(listen(sockfd, 32) == -1){
		std::cerr << "Socket unable to listen." << std::endl;
		return;
	}

#ifdef DEBUG
	std::cout << "Server established on: " << inet_ntoa(addr.sin_addr) << ":" << ntohs(addr.sin_port) << std::endl;
#endif

	std::thread thr_accept([&](){
		while(running){
#ifdef DEBUG
			std::cout << "Waiting for a client..." << std::endl;
#endif
			struct sockaddr_in clientAddr;
			socklen_t clientLen = sizeof(clientAddr);

			int client = accept(sockfd, (struct sockaddr *)&clientAddr, &clientLen);
			if(client == -1){
				std::cerr << "Accepting client error: " << errno << " (" << strerror(errno) << ")" << std::endl;
				return;
			}

#ifdef DEBUG
			std::cout << "Accepted client: " << client << std::endl;
#endif
			clientfd.push_back(client);
			std::thread clientThread(&tcpServer::listenToClient, this, client);
			clientThread.detach();
		}
	});
	thr_accept.detach();
}

tcpServer::~tcpServer(){
	for(unsigned long i = 0; i < clientfd.size(); i++)
		::close(clientfd[i]);
	::close(sockfd);
}

void tcpServer::listenToClient(int clientSock){
	int bytes = 0;
	char msg[1024];
	while(1){
		bytes = recv(clientSock, msg, 1024, 0);
		if(bytes == 0){
#ifdef DEBUG
			std::cout << "Client " << clientSock << " disconnected." << std::endl;
#endif
			close(clientSock);
			break;
		}
		msg[bytes] = 0;
		std::string response = callback_ptr(msg);
		if(response.length() > 0)
			send(clientSock, response.c_str(), response.length(), 0);
	}
}

void tcpServer::detach(){
	running = false;
}

int tcpServer::close(int fd){
	for(unsigned long i = 0; i < clientfd.size(); i++)
		if(clientfd[i] == fd){
			clientfd.erase(clientfd.begin() +i);
			break;
		}
	return ::close(fd);
}
