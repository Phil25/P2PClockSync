package p2pclocksync.net;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.BiConsumer;

import java.net.*;

public class UDPClient implements Closeable{

	private static final int LEN = 255;
	private static final int SO_TIMEOUT = 100;

	private int port;
	private DatagramSocket socket;
	private InetAddress broadcast;

	public UDPClient(int port) throws Exception{
		this(port, null);
	}

	public UDPClient(int port, InetAddress broadcast) throws Exception{
		this.port = port;
		this.broadcast = broadcast;
		socket = new DatagramSocket();
		socket.setSoTimeout(SO_TIMEOUT);
	}

	private void setBroadcast(boolean set){
		try{
			socket.setBroadcast(set);
		}catch(SocketException e){}
	}

	private boolean sendMessage(InetAddress address, String message){
		byte[] buffer = message.getBytes();
		try{
			socket.send(new DatagramPacket(buffer, buffer.length, address, port));
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private String receiveMessage(){
		byte[] buffer = new byte[LEN];
		DatagramPacket packet = new DatagramPacket(buffer, LEN);
		try{
			socket.receive(packet);
			return new String(packet.getData(), 0, packet.getLength());
		}catch(IOException e){
			return null;
		}
	}

	private void receiveBroadcast(BiConsumer<InetAddress, String> callback){
		byte[] buffer = new byte[LEN];
		DatagramPacket packet = new DatagramPacket(buffer, LEN);
		try{
			socket.receive(packet);
			String message = new String(packet.getData(), 0, packet.getLength());
			callback.accept(packet.getAddress(), message);
		}catch(IOException e){}
	}

	public String send(InetAddress address, String message){
		setBroadcast(false);
		sendMessage(address, message);
		return receiveMessage();
	}

	public void broadcast(String message, BiConsumer<InetAddress, String> callback, long timeout){
		if(broadcast == null)
			return;
		setBroadcast(true);
		sendMessage(broadcast, message);
		if(callback == null)
			return;
		new Thread(() -> {
			long until = System.currentTimeMillis() +timeout;
			while(System.currentTimeMillis() < until)
				receiveBroadcast(callback);
		}).start();
	}

	@Override
	public void close(){
		socket.close();
	}

}
