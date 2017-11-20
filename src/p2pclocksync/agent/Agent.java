package p2pclocksync.agent;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import p2pclocksync.data.AgentData;
import p2pclocksync.net.TCPClient;
import p2pclocksync.net.TCPServer;

public class Agent{

	private static AgentData thisData;
	private static ArrayList<AgentData> data;
	private static boolean showCounter = false;

	public static void main(String[] args){
		if(args.length < 2){
			System.err.println("Please specify initial clock value and port as arguments.");
			return;
		}

		thisData = new AgentData("localhost", Integer.parseInt(args[1]), Integer.parseInt(args[0]));
		data = new ArrayList<AgentData>();

		initCounterThread();
		initShowCounterThread();
		captureShutdown();
		createTcpServer();

		if(args.length > 3)
			setupSubAgent(args[2], Integer.parseInt(args[3]));

		handleUserCommands(); // blocks thread

		System.exit(0);
	}

	private static void initCounterThread(){
		new Thread(() -> {
			while(true){
				thisData.clock++;
				sleep(1);
			}
		}).start();
	}

	private static void initShowCounterThread(){
		new Thread(() -> {
			double time = 0.0;
			while(true){
				sleep(10);
				if(!showCounter)
					continue;
				time = thisData.clock /1000.0;
				System.out.printf("%.3f\n", time);
			}
		}).start();
	}

	private static void captureShutdown(){
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			String thisAddress = thisData.ip + ':' + thisData.port;
			for(int i = 0; i < data.size(); i++)
				data.get(i).send(thisAddress);
		}));
	}

	private static void createTcpServer(){
		TCPServer server = new TCPServer(thisData.port, (msg) -> processRequest(msg));
		System.out.println("Server on " + server + " initialized!");
	}

	private static void setupSubAgent(String initIp, int initPort){
		if(initPort == 0){
			System.err.println("Initial agent address is incorrect.");
			return;
		}
		addAddress(initIp, initPort);

		// 1. Download list of addresses of all agents
		addAddress(data.get(0).send("NET"));

		String thisAddress = thisData.ip + ':' + thisData.port;
		String otherClock = null;
		for(int i = 0; i < data.size(); i++){

			// 2. Send own address to all agents
			data.get(i).send(thisAddress);

			// 3. (1)Download clocks of other agents
			data.get(i).clock = Integer.parseInt(data.get(i).send("CLK"));
			System.out.println(i + ". " + data.get(i).clock);
		}

		// 3. (2)Update this agent's clock
		updateClock();

		// 4. Send SYN to all other agents
		for(int i = 0; i < data.size(); i++)
			data.get(i).send("SYN");
	}

	private static void handleUserCommands(){
		Scanner in = new Scanner(System.in);
		String cmd = null;
		while(true){
			cmd = in.nextLine();
			if(cmd.compareToIgnoreCase("exit") == 0 || cmd.compareToIgnoreCase("quit") == 0)
				break;
			System.out.print(processCommand(cmd));
		}
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
				for(int i = 0; i < len; i++)
					list = list + data.get(i).ip + ':' + data.get(i).port + (i == len -1 ? "" : ';');
				result = list;
				break;

			case "SYN":	// call for synchronization
				for(int i = 0; i < data.size(); i++){
					data.get(i).clock = Integer.parseInt(data.get(i).send("CLK"));
					System.out.println(i + ". " + data.get(i).clock);
				}
				updateClock();
				break;

			case "END": // end process
				System.exit(0);
				break;

			default:	// address to be added/removed
				String[] split = msg.split(":");
				if(split.length > 1)
					addAddress(split[0], Integer.parseInt(split[1]));
				break;
		}
		return result == null ? "" : result;
	}

	private static String processCommand(String cmd){
		String result = null;
		switch(cmd){
			case "show":
				showCounter = true;
				result = "Showing counter.\n";
				break;

			case "hide":
				showCounter = false;
				result = "Hiding counter.\n";
				break;

			case "list":
				int len = data.size();
				if(len == 0)
					result = "No agents reported.\n";
				else for(int i = 0; i < len; i++)
					result += data.get(i).ip + ':' + data.get(i).port + " -- " + data.get(i).clock + "ms\n";
				break;

			case "clock":
				result = "Current clock: " + thisData.clock;
				break;
		}
		return result == null ? ("Unknown command: " + cmd + "\n") : result;
	}

	private static void addAddress(String ip, int port){
		int i = 0, len = data.size();
		for(; i < len; i++)
			if(data.get(i).ip.compareTo(ip) == 0 && data.get(i).port == port)
				break;
		if(i == len)
			data.add(new AgentData(ip, port, 0, new TCPClient(ip, port)));
		else data.remove(i);
		System.out.println((i == len ? "Added" : "Removed") + " address: " + ip + ':' + port);
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

	private static void updateClock(){
		int i = 0;
		long clocks = thisData.clock;
		for(; i < data.size(); i++)
			clocks += data.get(i).clock;
		clocks /= i +1;
		System.out.println("Updating clock from " + thisData.clock + "ms to " + clocks + "ms.");
		thisData.clock = clocks;
	}

	private static void sleep(long x){
		try{
			Thread.sleep(x);
		}catch(InterruptedException e){}
	}

}
