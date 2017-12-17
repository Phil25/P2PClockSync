package p2pclocksync.net;

import java.io.IOException;
import java.util.function.Function;

import java.net.*;

public class UDPServer extends Thread{

	private static final int LEN = 255;

	private boolean running;
	private int port;
	private Function<String, String> func;
	private DatagramSocket socket;

	public UDPServer(int port, Function<String, String> func){
		running = true;
		this.port = port;
		this.func = func;
		try{
			socket = new DatagramSocket(port);
			this.start();
		}catch(SocketException e){
			e.printStackTrace();
		}
	}

	public void run(){
		DatagramPacket packet = null;
		byte[] buffer = new byte[LEN];
		while(running){
			packet = new DatagramPacket(buffer, LEN);
			try{
				socket.receive(packet);
			}catch(IOException e){
				packet.setData("receive error".getBytes());
			}
			String response = func.apply(new String(buffer, 0, packet.getLength()));
			if(response != null)
				reply(packet, response);
		}
		socket.close();
	}

	private void reply(DatagramPacket packet, String response){
		byte[] buffer = response.getBytes();
		try{
			InetAddress address = packet.getAddress();
			int port = packet.getPort();
			socket.send(new DatagramPacket(buffer, buffer.length, address, port));
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void detach(){
		running = false;
	}

	@Override
	public String toString(){
		return socket.toString();
	}

}
