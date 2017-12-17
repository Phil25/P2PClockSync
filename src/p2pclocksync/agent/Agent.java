package p2pclocksync.agent;

import java.net.*;
import java.util.*;

import p2pclocksync.data.CounterData;
import p2pclocksync.net.UDPClient;
import p2pclocksync.net.UDPServer;

public class Agent{

	private static int PORT = 22222;

	private static long initCounter, initPeriod;
	private static InetAddress broadcast;
	private static Agent agent;

	public UDPClient client;
	public long counter, period;
	public Map<Integer, CounterData> counters;
	public List<Integer> locals;

	public static void main(String[] args){
		if(!processArgs(args)){
			System.err.println("Please specify counter, period and broadcast address as arguments.");
			return;
		}
		try{
			agent = new Agent(initCounter, initPeriod);
		}catch(SocketException e){
			e.printStackTrace();
		}
	}

	public Agent(long counter, long period) throws SocketException{
		this.counter = counter;
		this.period = period;
		this.counters = new HashMap<Integer, CounterData>();
		this.locals = getLocalAddresses();
		initCounter();
		initPeriod();
		initServer();
		initClient();
	}

	private static boolean processArgs(String[] args){
		if(args.length < 3)
			return false;
		try{
			initCounter = Long.parseLong(args[0]);
			if((initPeriod = Long.parseLong(args[1]) *1000) == 0)
				throw new Exception();
			broadcast = InetAddress.getByName(args[2]);
		}catch(Exception e){
			return false;
		}
		return true;
	}

	private void initCounter(){
		new Thread(() -> {
			while(true){
				sleep(1);
				counter++;
			}
		}).start();
	}

	private void initPeriod(){
		new Thread(() -> {
			while(true){
				sleep(period);
				sync();
			}
		}).start();
	}

	private void initServer(){
		MessageProcessor mp = new MessageProcessor(this);
		UDPServer server = new UDPServer(PORT, msg -> mp.process(msg));
		System.out.println("Server running on port " + PORT + ".");
	}

	private void initClient(){
		try{
			client = new UDPClient(PORT, Agent.broadcast);
		}catch(Exception e){
			client = null;
		}
	}

	private void sync(){
		if(client == null)
			return;
		averageCurrent();
		counters.clear();
		client.broadcast("get counter", (addr, msg) -> processCounters(addr, msg), period);
	}

	private void averageCurrent(){
		Collection<CounterData> counters = this.counters.values();
		long sum = 0;
		for(CounterData counter : counters)
			sum += counter.getCounter();
		sum += this.counter;
		System.out.print(getCounter() + "ms -> ");
		this.counter = sum/(counters.size() +1);
		System.out.println(getCounter() + "ms");
	}

	private void processCounters(InetAddress address, String message){
		int hash = addressToInt(address);
		if(locals.contains(hash))
			return;
		try{
			long counter = Long.parseLong(message);
			counters.put(hash, new CounterData(counter));
		}catch(NumberFormatException e){
			System.err.println("Error reading counter from " + address.getHostAddress());
		}
	}

	private String getCounter(){
		long t = counter;
		long ms = t %1000;
		t = (t -ms) /1000;
		long sec = t %60;
		t = (t -sec) /60;
		long min = t %60;
		long h = (t -min) /60;
		return String.format("%02d:%02d:%02d.%03d", h, min, sec, ms);
	}

	private void sleep(long x){
		try{
			Thread.sleep(x);
		}catch(InterruptedException e){}
	}

	private List<Integer> getLocalAddresses() throws SocketException{
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		List<Integer> list = new ArrayList<Integer>();
		NetworkInterface i = null;
		while(interfaces.hasMoreElements() && isValidInterface((i = interfaces.nextElement()))){
			i.getInterfaceAddresses()
				.stream()
				.map(a -> a.getAddress())
				.filter(Objects::nonNull)
				.map(a -> addressToInt(a))
				.forEach(list::add);
		}
		return list;
	}

	private static boolean isValidInterface(NetworkInterface i){
		try{
			return i == null ? false : !i.isLoopback() && i.isUp();
		}catch(SocketException e){
			return false;
		}
	}

	private int addressToInt(InetAddress address){
		int result = 0;
		byte[] bytes = address.getAddress();
		for(int i = 0; i < bytes.length; i++)
			result |= ((bytes[i] & 0xFF) << (8 *i));
		return result;
	}

}
