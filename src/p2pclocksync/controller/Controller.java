package p2pclocksync.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import p2pclocksync.net.UDPClient;

public class Controller{

	private static InetAddress address;
	private static Scanner sc;
	private static String cmd, other;

	public static void main(String[] args){
		if(args.length > 0){
			other = args[0];
			address = getAddress(other);
			if(address == null){
				System.err.println("Unknown host; exiting...");
				System.exit(1);
			}
		}
		System.out.println("Controlling: " + address.getHostAddress());
		cmd = argsToLine(args);
		execute();
		sc = new Scanner(System.in);
		while(getLine() && execute());
		sc.close();
	}

	private static InetAddress getAddress(String address){
		try{
			return InetAddress.getByName(address);
		}catch(UnknownHostException e){
			return null;
		}
	}

	private static String argsToLine(String[] args){
		if(args.length < 2)
			return null;
		String line = "";
		for(int i = 1; i < args.length; i++)
			line += args[i] + " ";
		return line.trim();
	}

	private static boolean getLine(){
		System.out.print("Command: ");
		cmd = sc.nextLine();
		return cmd != null;
	}

	private static boolean execute(){
		if(cmd == null)
			return true;
		if(cmd.equalsIgnoreCase("exit") || cmd.equalsIgnoreCase("quit"))
			return false;
		String reply = UDPClient.send(address, cmd);
		if(reply != null)
			System.out.println(other + ": " + reply);
		return true;
	}

}
