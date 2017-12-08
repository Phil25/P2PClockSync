package p2pclocksync.net;

import java.io.IOException;
import java.util.function.BiFunction;

import java.net.*;

public class UDPServer extends Thread{

	private static final int BUFLEN = 255;

	private DatagramSocket sock;
	private BiFunction<String, String, String> func;
	private boolean running;

	public UDPServer(int port, BiFunction<String, String, String> func){
		try{
			this.sock = new DatagramSocket(port);
		}catch(SocketException e){
			return;
		}
		this.func = func;
		this.running = true;
		this.start();
	}

	public void run(){
		DatagramPacket packet = null;
		byte[] buffer = new byte[BUFLEN];
		while(running){
			packet = new DatagramPacket(buffer, BUFLEN);
			try{
				sock.receive(packet);
			}catch(IOException e){
				packet.setData("Receive error.".getBytes());
			}
			reply(packet, getResponse(packet));
		}
		sock.close();
	}

	private String getResponse(DatagramPacket packet){
		String response = new String(packet.getData(), 0, packet.getLength());
		return func.apply(packet.getAddress().getHostAddress(), response);
	}

	private void reply(DatagramPacket packet, String message){
		if(message == null)
			return;
		byte[] buffer = message.getBytes();
		try{
			sock.send(new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort()));
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void detach(){
		running = false;
	}

	@Override
	public String toString(){
		return sock.toString();
	}

}
