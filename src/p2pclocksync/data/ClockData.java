package p2pclocksync.data;

public class ClockData{

	private long clock, setTime;

	public ClockData(long clock){
		this.setClock(clock);
	}

	public void setClock(long clock){
		this.clock = clock;
		this.setTime = System.currentTimeMillis();
	}

	public long getClock(){
		return clock +System.currentTimeMillis() -setTime;
	}

}
