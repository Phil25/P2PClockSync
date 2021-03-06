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

import p2pclocksync.data.AgentData;
import p2pclocksync.net.TCPClient;

public class Monitor{

	private static int port = 3000;

	private static HttpServer server = null;
	private static ArrayList<AgentData> data = null;

	public static void main(String[] args){
		data = new ArrayList<AgentData>();
		if(args.length > 0)
			port = Integer.parseInt(args[0]);
		if(createHttpServer())
			System.out.println("HTTP server running on localhost:" + port);
	}

	private static boolean createHttpServer(){
		try{
			server = HttpServer.create(new InetSocketAddress(port), 0);
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
			if(response == null)
				response = "Unknown error occured.";
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
		if(params == null){
			response = readFile("./src/p2pclocksync/monitor/index.html");
		}else{
			if(params.containsKey("hostname")){
				addAgent(params);
			}else if(params.containsKey("table")){
				response = buildTable();
			}else if(params.containsKey("synchronize")){
				int id = Integer.parseInt((String)params.get("synchronize"));
				data.get(id).send("SYN");
			}else if(params.containsKey("remove")){
				int id = Integer.parseInt((String)params.get("remove"));
				data.remove(id);
			}else if(params.containsKey("end")){
				int id = Integer.parseInt((String)params.get("end"));
				data.get(id).send("END");
			}
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
		for(int i = 0; i < len; i++)
			data.get(i).clock = Integer.parseInt(data.get(i).send("CLK"));
		String table = "<tr><td>Address</td><td>Clock</td><td colspan=2>Options</td></tr>";
		for(int i = 0; i < len; i++)
			table
				+= "<tr><td>"
				+ getAgentAddress(i)
				+ "</td><td>"
				+ formatMs(data.get(i).clock)
				+ "</td><td>"
				+ "<button class=\"optionButton\" onmousedown=\"agentOption('synchronize=" + i + "')\">SYN</button>"
				+ "</td><td>"
				+ "<button class=\"optionButton\" onmousedown=\"agentOption('end=" + i + "')\">END</button>"
				+ "</td><td>"
				+ "<button class=\"optionButton\" onmousedown=\"agentOption('remove=" + i + "')\">x</button>"
				+ "</td></tr>";
		return table;
	}

	private static String formatMs(long t){
		if(t == 0)
			return "unconnected";
		long ms = t %1000;
		t = (t -ms) /1000;
		long sec = t %60;
		t = (t -sec) /60;
		long min = t %60;
		long h = (t -min) /60;
		return String.format("%02d:%02d:%02d.%03d", h, min, sec, ms);
	}

	private static void addAgent(HashMap<String, String> params){
		if(!params.containsKey("port"))
			return;
		String hostname = params.get("hostname");
		int port = Integer.parseInt((String)(params.get("port")));
		data.add(new AgentData(hostname, port, 0, new TCPClient(hostname, port)));
	}

	private static void remAgent(int i){
		if(0 <= i && i < data.size())
			data.remove(i);
	}

	private static String getAgentAddress(int i){
		return data.get(i).ip + ':' + data.get(i).port;
	}

	private static void sleep(long x){
		try{
			Thread.sleep(x);
		}catch(InterruptedException e){}
	}

}
