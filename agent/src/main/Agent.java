package main;

import java.util.ArrayList;

class AgentData{
	public String ip;
	public int port;
	public long clock;

	public AgentData(String ip, int port, long clock){
		this.ip = ip;
		this.port = port;
		this.clock = clock;
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
		thisData = new AgentData(args[0], Integer.parseInt(args[1]), 0);
		System.out.println(args.length);
	}

}
