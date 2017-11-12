package p2pclocksync.net;

import java.io.IOException;
import java.util.function.Function;

import java.net.*;

public class TCPServer{

	private ServerSocket sock;
	private boolean running = true;

	public TCPServer(int port, Function<String, String> func){
		try{
		sock = new ServerSocket(port);
		Runnable listener = () -> {
			try{
				while(running){
					Socket conn = sock.accept();
					new ListenThread(conn, func).start();
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

	public void detach(){
		running = false;
	}

	@Override
	public String toString(){
		return sock.getLocalSocketAddress().toString();
	}

}
