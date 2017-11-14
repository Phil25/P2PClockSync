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

import p2pclocksync.net.TCPClient;
import p2pclocksync.data.AgentData;

public class Monitor{

	private static final int PORT = 3000;
	private static final int UPDATE_INTERVAL = 250;
	private static final String REFRESH_HEADER = "<meta http-equiv=\"refresh\" content=\"0; url=http://localhost:" + PORT + "\">";

	private static HttpServer server = null;
	private static ArrayList<AgentData> data = null;

	public static void main(String[] args){
		data = new ArrayList<AgentData>();
		if(createHttpServer())
			System.out.println("HTTP server running on localhost:" + PORT);
		processUserCommands(); // blocking
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
			response = response.replaceAll("\\$REFRESH_HEADER\\$", (params != null && params.containsKey("hostname")) ? REFRESH_HEADER : "");
			response = response.replaceAll("\\$UPDATE_INTERVAL\\$", "" + UPDATE_INTERVAL);
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
		String table = "<tr><th>Address</th><th>Clock</th></tr>";
		for(int i = 0; i < len; i++)
			data.get(i).clock = Integer.parseInt(data.get(i).send("CLK"));
		for(int i = 0; i < len; i++)
			table += "<tr><th>" + data.get(i).ip + ':' + data.get(i).port + "</th><th>" + data.get(i).clock + "ms</th></tr>";
		return table;
	}

	private static void processUserCommands(){
		Scanner in = new Scanner(System.in);
		while(true)
			processCommand(in.nextLine());
	}

	private static void processCommand(String cmd){
		if(cmd == null)
			return;
		String[] split = cmd.split(" ");
		if(split.length < 2)
			return;
		String ip = split[0];
		int port = Integer.parseInt(split[1]);
		data.add(new AgentData(ip, port, 0, new TCPClient(ip, port)));
/*		try{
			Process agent = Runtime.getRuntime().exec("java -cp bin p2pclocksync.agent.Agent 0" + port);
			if(agent.isAlive())
				System.out.println("Agent ran");
		}catch(IOException e){
			System.err.println("Could not launch agent: " + e.getCause());
			return;
		}*/
	}

}
