package p2pclocksync.data;

public class NetData{

	List<InetAddress> internals, broadcasts;

	static{
		internals = fetchInternalAddresses();
		broadcasts = fetchBroadcastAddresses();
	}

	private NetData(){}

	private List<InetAddress> fetchInternalAddresses(){
	}

	private List<InetAddress> fetchBroadcastAddresses(){
		try{
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
		}catch(SocketException e){
			return null;
		}
	}

}
