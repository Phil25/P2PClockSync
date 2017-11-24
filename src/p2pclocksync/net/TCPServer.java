package p2pclocksync.net;

import java.io.IOException;
import java.util.Enumeration;
import java.util.function.Function;

import java.net.*;

public class TCPServer{

	private ServerSocket sock;
	private boolean running = true;
	private String hostname = "0.0.0.0";

	public TCPServer(int port, Function<String, String> func){
		try{
		sock = new ServerSocket(port);
		this.hostname = findHostname();
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

	public String getHostname(){
		return hostname;
	}

	private String findHostname(){
		try{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while(interfaces.hasMoreElements()){
				NetworkInterface iface = interfaces.nextElement();
				if(iface.isLoopback() || !iface.isUp())
					continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while(addresses.hasMoreElements()){
					InetAddress addr = addresses.nextElement();
					if(addr instanceof Inet4Address)
						return addr.getHostAddress();
				}
			}
		}catch(SocketException e){}
		return "127.0.0.1";
	}

	@Override
	public String toString(){
		return sock.toString();
	}

}
