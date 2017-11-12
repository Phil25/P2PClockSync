package p2pclocksync.monitor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Scanner;

import p2pclocksync.data.AgentData;

public class Monitor{

	static class RequestHandler implements HttpHandler{
		@Override
		public void handle(HttpExchange t) throws IOException{
			String resp = "<head><meta http-equiv=\"refresh\" content=\"1\" /></head><h1>Sockets:</h1>";
			resp = buildTable(resp);
			t.sendResponseHeaders(200, resp.length());
			OutputStream os = t.getResponseBody();
			os.write(resp.getBytes());
			os.close();
		}

		private String buildTable(String resp){
			int len = data.size();
			if(len == 0)
				return resp += "No agents registered.";
			for(int i = 0; i < len; i++)
				resp += data.get(i).ip + " " + data.get(i).port + " -- " + data.get(i).clock + "ms <br />";
			return resp;
		}
	}

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
		server.createContext("/", new RequestHandler());
		server.setExecutor(null);
		server.start();
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
