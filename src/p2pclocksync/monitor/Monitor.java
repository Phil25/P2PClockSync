package p2pclocksync.monitor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import p2pclocksync.data.AgentData;
import p2pclocksync.net.TCPClient;

public class Monitor{

	private static final int PORT = 3000;

	private static String initIp = null;
	private static int initPort = 0;

	private static HttpServer server = null;
	private static ArrayList<AgentData> data = null;

	public static void main(String[] args){
		data = new ArrayList<AgentData>();
		if(createHttpServer())
			System.out.println("HTTP server running on localhost:" + PORT);
	}

	private static boolean createHttpServer(){
		try{
			server = HttpServer.create(new InetSocketAddress(PORT), 0);
		}catch(IOException e){
			System.err.println("Could not create HTTP server: " + e.getCause());
			return false;
		}
		server.createContext("/", (HttpExchange t) -> httpHandler(t));
		server.setExecutor(null);
		server.start();
		return true;
	}

	private static void httpHandler(HttpExchange he){
		try{
			HashMap<String, String> params = getParams(he.getRequestURI().toString().split("(\\?|\\&)"));
			String response = processParams(params);
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}catch(IOException e){}
	}

	private static HashMap<String, String> getParams(String[] params){
		if(params.length < 2)
			return null;
		HashMap<String, String> map = new HashMap<String, String>();
		String[] split = null;
		for(int i = 1; i < params.length; i++){
			split = params[i].split("=");
			map.put(split[0], split[1]);
		}
		return map;
	}

	private static String processParams(HashMap<String, String> params){
		String response = null;
		if(params != null && params.containsKey("table")){
			response = buildTable();
		}else{
			response = readFile("./src/p2pclocksync/monitor/index.html");
			if(params != null && params.containsKey("hostname"))
				newAgent(params);
		}
		return response;
	}

	private static String readFile(String path){
		try{
			BufferedReader br = new BufferedReader(new FileReader(path));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while(line != null){
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		}catch(FileNotFoundException e){
			System.err.println("Could not find " + path);
		}catch(IOException e){
			System.err.println("Could not read " + path + ": " + e.getCause());
		}
		return null;
	}

	private static String buildTable(){
		int len = data.size();
		if(len == 0)
			return "<tr><th>No agents registered.</th><tr>";
		String table = "<tr><th>Address</th><th>Clock</th>";
		for(int i = 0; i < len; i++)
			data.get(i).clock = Integer.parseInt(data.get(i).send("CLK"));
		for(int i = 0; i < len; i++)
			table += "<tr><th>" + data.get(i).ip + ':' + data.get(i).port + "</th><th>" + data.get(i).clock + "ms</th></tr>";
		return table;
	}

	//private static String getRemoveButton(int i){
	//	return "<input type=\"button\" value=\"stop\" onclick=\"remAgent(\'" + data.get(i).ip + ':' + data.get(i).port + "\')\"></tr>";
	//}

	private static void newAgent(HashMap<String, String> params){
		if(!params.containsKey("port") || !params.containsKey("clock"))
			return;
		String hostname = params.get("hostname");
		int port = Integer.parseInt((String)(params.get("port")));
		int clock = Integer.parseInt((String)(params.get("clock")));
		if(!execAgent(port, clock))
			return;
		sleep(250); // wait for the agent to set up a server
		initIp = hostname;
		initPort = port;
		data.add(new AgentData(hostname, port, clock, new TCPClient(hostname, port)));
	}

	private static boolean execAgent(int port, int clock){
		try{
			String launcher = "java -cp bin p2pclocksync.agent.Agent " + clock + " " + port;
			if(initIp != null)
				launcher += " " + initIp + " " + initPort;
			Process agent = Runtime.getRuntime().exec(launcher);
			return agent.isAlive();
		}catch(IOException e){
			System.out.println("Could not initialize agent: " + e.getCause());
		}
		return false;
	}

	private static void sleep(long x){
		try{
			Thread.sleep(x);
		}catch(InterruptedException e){}
	}

}
