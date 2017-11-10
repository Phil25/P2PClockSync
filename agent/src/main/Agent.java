package main;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

class AgentData{
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

	public boolean compare(AgentData other){
		return
			this.ip == other.ip &&
			this.port == other.port;
	}
}

public class Agent{

	private static AgentData thisData;
	private static ArrayList<AgentData> data;

	public static void main(String[] args){
		if(args.length < 2){
			System.err.println("Please specify initial clock value and port as arguments.");
			return;
		}

		thisData = new AgentData("127.0.0.1", Integer.parseInt(args[1]), Integer.parseInt(args[0]));
		data = new ArrayList<AgentData>();

		TCPServer server = new TCPServer(thisData.port, (msg) -> processRequest(msg));
		System.out.println("Server on " + server + " initialized!");

		if(args.length > 3) // Initialization agent's address specified
			setupSubAgent(args[2], Integer.parseInt(args[3]));
		else data.add(new AgentData(thisData.ip, thisData.port, 0));
	}

	private static void setupSubAgent(String initIp, int initPort){
		if(initPort == 0){
			System.err.println("Initial agent address is incorrect.");
			return;
		}
		addAddress(initIp, initPort);
		addAddress(data.get(0).send("NET"));
		String thisAddress = thisData.ip + ':' + thisData.port;
		for(int i = 0; i < data.size(); i++)
			data.get(i).send(thisAddress);
	}

	private static String processRequest(String msg){
		String result = null;
		switch(msg){
			case "CLK": // request agents' clock
				result = "" + thisData.clock;
				break;

			case "NET":	// request agents' addresses
				String list = "";
				int len = data.size();
				for(int i = 1; i < len; i++)
					list = list + data.get(i).ip + ':' + data.get(i).port + (i == len -1 ? "" : ';');
				result = list;
				break;

			case "SYN":	// call for synchronization
				System.out.println("TODO: SYN");
				break;

			default:	// address to be added/removed
				String[] split = msg.split(":");
				if(split.length > 1)
					addAddress(split[0], Integer.parseInt(split[1]));
				break;
		}
		return result == null ? null : result.length() > 0 ? result : null;
	}

	private static void addAddress(String ip, int port){
		for(int i = 0; i < data.size(); i++)
			if(data.get(i).ip == ip && data.get(i).port == port)
				return;
		data.add(new AgentData(ip, port, 0, new TCPClient(ip, port)));
		System.out.println("Added address: " + ip + ':' + port);
	}

	private static void addAddress(String list){
		if(list == null || list.length() == 0)
			return;
		String[] addresses = list.split(";");
		String[] split = null;
		for(int i = 0; i < addresses.length; i++){
			split = addresses[i].split(":");
			if(split.length > 1)
				addAddress(split[0], Integer.parseInt(split[1]));
		}
	}

}
