package p2pclocksync.monitor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import p2pclocksync.data.AgentData;

public class Monitor{

	private static final int PORT = 3000;
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
			System.out.println("Putting: " + split[0] + ", " + split[1]);
		}
		return map;
	}

	private static String processParams(HashMap<String, String> params){
		String response = null;
		if(params != null && params.containsKey("format")){
			response = readFile("./src/p2pclocksync/monitor/data.json");
		}else{
			response = readFile("./src/p2pclocksync/monitor/index.html");
			response = response.replaceAll("\\$AGENT_TABLE\\$", buildTable());
			response = response.replaceAll("\\$PORT\\$", "" + PORT);
			response = response.replaceAll("\\$REFRESH_HEADER\\$", (params != null && params.containsKey("hostname")) ? REFRESH_HEADER : "");
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
			return "No agents registered.";
		String table = "<table style=\"width:20em\"><tr><th>Address</th><th>Clock</th></tr>";
		for(int i = 0; i < len; i++)
			table += "<tr><th>" + data.get(i).ip + ':' + data.get(i).port + "</th><th>" + data.get(i).clock + "ms</th></tr>";
		return table + "</table>";
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
		if(split.length < 3)
			return;
		data.add(new AgentData(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2])));
	}

}
