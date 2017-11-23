package p2pclocksync.net;

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

	private BufferedReader in;
	private BufferedWriter out;

	public ListenThread(Socket socket, Function<String, String> func){
		this.socket = socket;
		this.func = func;
		this.in = null;
		this.out = null;
	}

	public void run(){
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
					break;
				}else{
					String result = func.apply(line);
					reply(result);
				}
			}catch(IOException e){
				break;
			}
		}
	}

	private void reply(String message){
		try{
			out.write(message);
			out.newLine();
			out.flush();
		}catch(IOException e){}
	}

}
