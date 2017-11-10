package main;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient implements Closeable{

	private Socket socket = null;
	private boolean valid = false;
	private PrintWriter out = null;
	private BufferedReader in = null;

	public TCPClient(String hostname, int port){
		try{
			socket = new Socket(hostname, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			valid = true;
		}catch(UnknownHostException e){
			System.err.println("Unknown host: " + hostname + ':' + port);
		}catch(IOException e){
			System.err.println("Error connecting to " + hostname + ':' + port);
		}
	}

	public String send(String msg){
		if(!valid)
			return null;
		out.println(msg);
		String result = null;
		try{
			result = in.readLine();
		}catch(IOException e){}
		return result;
	}

	@Override
	public void close(){
		try{
			socket.close();
			in.close();
			out.close();
		}catch(IOException e){}
		valid = false;
	}

}
