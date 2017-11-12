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
import java.util.Scanner;

import p2pclocksync.data.AgentData;

public class Monitor{

	private static HttpServer server = null;
	private static ArrayList<AgentData> data = null;

	public static void main(String[] args){
		data = new ArrayList<AgentData>();
		createHttpServer();
		processUserCommands(); // blocking
	}

	private static void createHttpServer(){
		try{
			server = HttpServer.create(new InetSocketAddress(3000), 0);
		}catch(IOException e){
			System.err.println("Could not create HTTP server: " + e.getCause());
			return;
		}
		server.createContext("/", (HttpExchange t) -> httpHandler(t));
		server.setExecutor(null);
		server.start();
	}

	private static void httpHandler(HttpExchange t){
		try{
			String response = readFile("./src/p2pclocksync/monitor/index.html");
			response = response.replaceAll("\\$AGENT_TABLE\\$", buildTable());
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}catch(IOException e){}
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
		String table = "";
		for(int i = 0; i < len; i++)
			table += data.get(i).ip + ':' + data.get(i).port + " -- " + data.get(i).clock + "ms <br />";
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
		if(split.length < 3)
			return;
		data.add(new AgentData(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2])));
	}

}
