package p2pclocksync.agent;

import java.net.InetAddress;

public class MessageProcessor{

	private Agent agent;

	public MessageProcessor(Agent agent){
		this.agent = agent;
	}

	public String process(String message){
		return message == null ? null : processMessage(message.toLowerCase().split(" "));
	}

	private String processMessage(String[] args){
		switch(args[0]){
			case "get": return processGet(args);
			case "set": return processSet(args);
		}
		return null;
	}

	private String processGet(String[] args){
		if(args.length >= 2)
			switch(args[1]){
				case "counter": return "" + agent.counter;
				case "period": return "" + agent.period;
			}
		return "invalid get arguments";
	}

	private String processSet(String[] args){
		if(args.length >= 3)
			switch(args[1]){
				case "counter":
				case "clock":
					return setCounter(args[2]);
				case "period": return setPeriod(args[2]);
			}
		return "invalid set arguments";
	}

	private String setCounter(String to){
		String response = "counter set from " + agent.counter;
		try{
			agent.counter = Long.parseLong(to);
		}catch(NumberFormatException e){
			return "invalid counter";
		}
		return response + " to " + to;
	}

	private String setPeriod(String to){
		String response = "period set from " + agent.period;
		try{
			agent.period = Long.parseLong(to);
		}catch(NumberFormatException e){
			return "invalid period";
		}
		return response + " to " + to;
	}

}
