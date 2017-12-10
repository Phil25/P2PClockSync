package p2pclocksync.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import java.net.*;

public class UDPClient{

	private static int BUFLEN = 255;

	private static DatagramSocket sock = null;
	private static InetAddress address = null;
	private static int port = 22222;
	private static List<InetAddress> broadcasts = null;

	static{
		try{
			sock = new DatagramSocket();
			broadcasts = getBroadcastAddresses();
		}catch(SocketException e){}
	}

	private UDPClient(){}

	public static void setPort(int newPort){
		port = newPort;
	}

	public static void broadcast(String message){
		try{
			sock.setBroadcast(true);
		}catch(SocketException e){
			return;
		}
		byte[] buffer = message.getBytes();
		try{
			for(InetAddress address : broadcasts)
				sock.send(new DatagramPacket(buffer, buffer.length, address, port));
		}catch(IOException e){}
	}

	public static String send(String ip, String message){
		try{
			return send(InetAddress.getByName(ip), message);
		}catch(UnknownHostException e){
			return null;
		}
	}

	public static String send(InetAddress address, String message){
		try{
			sock.setBroadcast(false);
		}catch(SocketException e){
			return null;
		}
		try{
			sock.send(buildPacket(address, message));
		}catch(IOException e){
			return null;
		}
		return getResponse();
	}

	private static DatagramPacket buildPacket(InetAddress address, String message){
		byte[] buffer = message.getBytes();
		return new DatagramPacket(buffer, buffer.length, address, port);
	}

	private static String getResponse(){
		byte[] buffer = new byte[BUFLEN];
		DatagramPacket packet = new DatagramPacket(buffer, BUFLEN);
		try{
			sock.receive(packet);
		}catch(IOException e){
			packet.setData("Response read error.".getBytes());
		}
		return new String(packet.getData(), 0, packet.getLength());
	}

	private static List<InetAddress> getBroadcastAddresses() throws SocketException{
		List<InetAddress> list = new ArrayList<InetAddress>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while(interfaces.hasMoreElements()){
			NetworkInterface i = interfaces.nextElement();
			if(i.isLoopback() || !i.isUp())
				continue;
			i.getInterfaceAddresses()
				.stream()
				.map(b -> b.getBroadcast())
				.filter(Objects::nonNull)
				.forEach(list::add);
		}
		return list;
	}

}
