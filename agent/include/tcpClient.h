#ifndef TCPCLIENT_H
#define TCPCLIENT_H

#include <string>

class tcpClient{
	std::string			ip;
	int					sock, port;
	bool				connected;

public:
	tcpClient(std::string, int);
	~tcpClient();
	bool send(std::string);
	std::string recv(size_t=4096);

};

#endif
