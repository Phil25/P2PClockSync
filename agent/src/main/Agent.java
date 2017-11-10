package main;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

class AgentData{
	public String ip;
	public int port;
	public long clock;
	public Socket socket;

	public AgentData(String ip, int port, long clock){
		this(ip, port, clock, null);
	}

	public AgentData(String ip, int port, long clock, Socket socket){
		this.ip = ip;
		this.port = port;
		this.clock = clock;
		this.socket = socket;
	}

	public boolean compare(AgentData other){
		return
			this.ip == other.ip &&
			this.port == other.port;
	}
}

public class Agent{

	private static final int CLK = 0;	// clock
	private static final int NET = 1;	// request agents' addresses
	private static final int SYN = 2;	// call for synchronization
	private static final int ADR = 3;	// address to be added/removed

	private static AgentData thisData;
	private static ArrayList<AgentData> data;

	public static void main(String[] args){
		if(args.length < 2){
			System.err.println("Please specify initial clock value and port as arguments.");
			return;
		}

		thisData = new AgentData("127.0.0.1", Integer.parseInt(args[1]), Integer.parseInt(args[0]));
		data = new ArrayList<AgentData>();
		if(args.length > 3) // Initialization agent's address specified
			setupSubAgent(args[2], Integer.parseInt(args[3]));
		else addAddress(thisData.ip, thisData.port);

		TCPServer server = new TCPServer(thisData.port, (msg) -> processRequest(msg));
		System.out.println("Server on " + server + " initialized!");
	}

	private static void setupSubAgent(String ip, int port){
		if(port == 0){
			System.err.println("Initial agent address is incorrect.");
			return;
		}
		addAddress(ip, port);
	}

	private static String processRequest(String msg){
		return "pong: " + msg;
	}

	private static void addAddress(String ip, int port){
		for(int i = 0; i < data.size(); i++)
			if(data.get(i).ip == ip && data.get(i).port == port)
				return;
		data.add(new AgentData(ip, port, 0, getSocket(ip, port)));
	}

	private static Socket getSocket(String ip, int port){
		try{
			Socket socket = new Socket(ip, port);
			return socket;
		}catch(UnknownHostException e){
			System.err.println("Unknown host: " + ip + ":" + port);
		}catch(IOException e){
			System.err.println("Error connecting to " + ip + ":" + port);
		}
		return null;
	}

}
