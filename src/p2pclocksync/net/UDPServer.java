package p2pclocksync.net;

import java.io.IOException;
import java.util.function.BiFunction;

import java.net.*;

public class UDPServer extends Thread{

	private static final int BUFLEN = 255;

	private DatagramSocket sock;
	private BiFunction<String, String, String> func;
	private boolean running;
	private int port;

	public UDPServer(int port, BiFunction<String, String, String> func){
		this.port = port;
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
		String incoming = new String(packet.getData(), 0, packet.getLength());
		return func.apply(packet.getAddress().getHostAddress(), incoming);
	}

	private void reply(DatagramPacket packet, String response){
		if(response == null)
			return;
		byte[] buffer = response.getBytes();
		try{
			sock.send(new DatagramPacket(buffer, buffer.length, packet.getAddress(), port));
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
