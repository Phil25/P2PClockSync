package p2pclocksync.data;

import p2pclocksync.net.TCPClient;

public class AgentData{
	public String ip;
	public int port;
	public long clock;
	public TCPClient client;

	public AgentData(String ip, int port, long clock){
		this(ip, port, clock, null);
	}

	public AgentData(String ip, int port, long clock, TCPClient client){
		this.ip = ip;
		this.port = port;
		this.clock = clock;
		this.client = client;
	}

	public String send(String msg){
		return this.client == null ? null : this.client.send(msg);
	}
}
