#ifndef TCPSERVER_H
#define TCPSERVER_H

#include <string>
#include <vector>
#include <arpa/inet.h>

class tcpServer{
	int sockfd;
	std::string (*callback_ptr)(std::string);
	struct sockaddr_in addr;
	bool running;
	std::vector<int> clientfd;

public:
	tcpServer(std::string, int, std::string (*callback_ptr)(std::string));
	~tcpServer();
	void detach();

private:
	void listenToClient(int);
	int close(int);

};

#endif
