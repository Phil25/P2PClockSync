package p2pclocksync.agent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import p2pclocksync.data.ClockData;
import p2pclocksync.net.UDPClient;
import p2pclocksync.net.UDPServer;

public class Agent{

	private static final int PORT = 22222;

	private static long clock, period;
	private static Map<String, ClockData> otherClocks;

	public static void main(String[] args){
		if(args.length < 2){
			System.err.println("Please specify initial clock value and sync interval as arguments.");
			return;
		}

		otherClocks = new HashMap<String, ClockData>();
		UDPClient.setPort(PORT);
		initCounterThread(Integer.parseInt(args[0]));
		createUdpServer();
		initSyncThread(Integer.parseInt(args[1]) *1000);
	}

	private static void initCounterThread(long initClock){
		clock = initClock;
		new Thread(() -> {
			while(true){
				clock++;
				sleep(1);
			}
		}).start();
	}

	private static void createUdpServer(){
		UDPServer server = new UDPServer(PORT, (ip, msg) -> processMessage(ip, msg));
		System.out.println("Server on port " + PORT + " initialized!");
	}

	private static void initSyncThread(long initPeriod){
		period = initPeriod;
		new Thread(() -> {
			while(true){
				syncCounters();
				UDPClient.broadcast("get counter");
				sleep(period);
			}
		}).start();
	}

	private static String processMessage(String ip, String msg){
		System.out.println("Received: " + msg + " from " + ip);
		String sending = msg == null ? null : processParams(ip, msg.split(" "));
		System.out.println("Sending: " + sending); 
		return sending;
	}

	private static String processParams(String ip, String[] params){
		switch(params[0]){
			case "get": return processGet(params);
			case "set": return processSet(params);
			default: setClockFor(ip, params[0]); // from broadcast
		}
		return null;
	}

	private static String processGet(String[] params){
		if(params.length < 2)
			return null;
		return params[1].equalsIgnoreCase("counter") ? getClock() : getPeriod();
	}

	private static String processSet(String[] params){
		if(params.length < 3)
			return null;
		return params[1].equals("counter") ? setClock(params[2]) : setPeriod(params[2]);
	}

	private static String getClock(){
		return "" + clock;
	}

	private static String getPeriod(){
		return "" + (period /1000);
	}

	private static String setClock(String newClock){
		try{
			clock = Long.parseLong(newClock);
		}catch(NumberFormatException e){
			return "invalid clock: " + newClock;
		}
		return "clock set";
	}

	private static void setClockFor(String ip, String newClock){
		long otherClock = -1;
		try{
			otherClock = Long.parseLong(newClock);
		}catch(NumberFormatException e){
			return;
		}
		otherClocks.put(ip, new ClockData(otherClock));
	}

	private static String setPeriod(String newPeriod){
		try{
			period = Long.parseLong(newPeriod) *1000;
		}catch(NumberFormatException e){
			return "invalid period: " + newPeriod;
		}
		return "period set";
	}

	private static void syncCounters(){
		Collection<ClockData> clocks = otherClocks.values();
		int sum = 0;
		for(ClockData otherClock : clocks)
			sum += otherClock.getClock();
		sum += clock;
		System.out.print(clock + "ms -> ");
		clock = sum /(clocks.size() +1);
		System.out.print(clock + "ms.\n");
	}

	private static void sleep(long x){
		try{
			Thread.sleep(x);
		}catch(InterruptedException e){}
	}

}
