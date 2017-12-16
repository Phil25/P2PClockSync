package p2pclocksync.data;

public class CounterData{

	private long counter, setTime;

	public CounterData(long counter){
		this.setCounter(counter);
	}

	public void setCounter(long counter){
		this.counter = counter;
		this.setTime = System.currentTimeMillis();
	}

	public long getCounter(){
		return this.counter +System.currentTimeMillis() -setTime;
	}

}
