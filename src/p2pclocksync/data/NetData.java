package p2pclocksync.data;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NetData{

	private static Enumeration<NetworkInterface> interfaces;
	private static List<InetAddress> internals, broadcasts;

	static{
		interfaces = fetchInterfaces();
		internals = fetchInternalAddresses();
		broadcasts = fetchBroadcastAddresses();
	}

	private NetData(){}

	public static List<InetAddress> getInternals(){
		return internals;
	}

	public static List<String> getInternalIps(){
		return internals
			.stream()
			.map(i -> i.getHostAddress())
			.collect(Collectors.toList());
	}

	public static List<InetAddress> getBroadcasts(){
		return broadcasts;
	}

	private static Enumeration<NetworkInterface> fetchInterfaces(){
		try{
			return NetworkInterface.getNetworkInterfaces();
		}catch(SocketException e){
			return null;
		}
	}

	private static List<InetAddress> fetchInternalAddresses(){
		List<InetAddress> list = new ArrayList<InetAddress>();
		NetworkInterface i = null;
		while(interfaces.hasMoreElements() && isValid((i = interfaces.nextElement()))){
			i.getInterfaceAddresses()
				.stream()
				.map(b -> b.getAddress())
				.filter(Objects::nonNull)
				.forEach(list::add);
		}
		return list;
	}

	private static List<InetAddress> fetchBroadcastAddresses(){
		List<InetAddress> list = new ArrayList<InetAddress>();
		NetworkInterface i = null;
		while(interfaces.hasMoreElements() && isValid((i = interfaces.nextElement()))){
			i.getInterfaceAddresses()
				.stream()
				.map(b -> b.getBroadcast())
				.filter(Objects::nonNull)
				.forEach(list::add);
		}
		return list;
	}

	private static boolean isValid(NetworkInterface i){
		try{
			return i == null ? false : !i.isLoopback() && i.isUp();
		}catch(SocketException e){
			return false;
		}
	}

}
