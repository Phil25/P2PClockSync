package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.function.Function;

public class ListenThread extends Thread{

	protected Socket socket;
	private Function<String, String> func;

	public ListenThread(Socket socket, Function<String, String> func){
		this.socket = socket;
		this.func = func;
	}

	public void run(){
		BufferedReader in = null;
		BufferedWriter out = null;
		try{
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		}catch(IOException e){
			return;
		}
		String line = null;
		while(true){
			try{
				line = in.readLine();
				if(line == null){
					socket.close();
					return;
				}else{
					System.out.println("INCOMING: " + line);
					String result = func.apply(line);
					System.out.println("RESPONSE: " + result);
					out.write(result);
					out.newLine();
					out.flush();
				}
			}catch(IOException e){
				return;
			}
		}
	}

}
