package main;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
		DataOutputStream out = null;
		try{
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
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
					String result = func.apply(line);
					out.writeBytes(result + "\n\r");
					out.flush();
				}
			}catch(IOException e){
				return;
			}
		}
	}

}
