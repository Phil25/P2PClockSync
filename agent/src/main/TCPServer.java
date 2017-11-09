package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.function.Function;

import java.net.*;

public class TCPServer{

	private ServerSocket sock;

	public TCPServer(int port, Function<String, String> func){
		try{
		sock = new ServerSocket(port);
		Runnable listener = () -> {
			try{
			while(true){
				Socket conn = sock.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String response = func.apply(in.readLine());
				if(response != null){
					PrintWriter out = new PrintWriter(conn.getOutputStream());
					out.println(response);
					out.close();
				}
				in.close();
				conn.close();
			}
			}catch(IOException e){
				System.err.println(e);
			}
		};
		new Thread(listener).start();
		}catch(IOException e){
			System.err.println("Error creating socket: " + e.getCause());
			return;
		}
	}

	@Override
	public String toString(){
		return sock.getLocalSocketAddress().toString();
	}

}
