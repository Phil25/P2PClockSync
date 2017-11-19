package p2pclocksync.net;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient implements Closeable{

	private Socket socket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;

	private boolean valid = false;
	private String hostname;
	private int port;

	public TCPClient(String hostname, int port){
		valid = false;
		this.hostname = hostname;
		this.port = port;
		connect();
		if(!valid)
			retryLoop();
	}

	private void connect(){
		try{
			socket = new Socket(hostname, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			valid = true;
		}catch(UnknownHostException e){
			System.err.println("Unknown host: " + hostname + ':' + port);
		}catch(IOException e){
//			System.err.println("Error connecting to " + hostname + ':' + port);
		}
	}

	private void retryLoop(){
		new Thread(() -> {
			while(!valid){
				sleep(3000);
				connect();
			}
		}).start();
	}

	public String send(String msg){
		if(!valid)
			return "unconnected";
		out.println(msg);
		String result = null;
		try{
			result = in.readLine();
		}catch(IOException e){
			result = "" + e.getCause();
		}
		if(result == null){
			valid = false;
			retryLoop();
		}
		return result;
	}

	private void sleep(long x){
		try{
			Thread.sleep(x);
		}catch(InterruptedException e){}
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
