import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class MyThread extends Thread{
	
	private MyThread next = null;
	private Integer myId;
	private int number;
	private CyclicBarrier barrier;
	private int counter = 0;
	private int maxThreads;
	
	public MyThread(Integer name, int number, CyclicBarrier barrier, int maxThreads) {
		myId = name;
		this.number = number;
		this.barrier = barrier;
		this.maxThreads = maxThreads;
	}
	
	@Override
	public void run() {
		while(counter < maxThreads) {
			try {
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(counter % 2 == myId % 2 && next != null) {
				if(number > next.getNumber()) {
					swap(number, next.getNumber());
				}
			}
			++counter;
			try {
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void swap(int myNumber, int nextNumber) {
		number = nextNumber;
		next.setNumber(myNumber);
	}
	
	public MyThread getNext() {
		return next;
	}
	public void setNext(MyThread next) {
		this.next = next;
	}
	public Integer getMyId() {
		return myId;
	}
	public void setMyId(Integer myId) {
		this.myId = myId;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public CyclicBarrier getBarrier() {
		return barrier;
	}
	public void setBarrier(CyclicBarrier barrier) {
		this.barrier = barrier;
	}
	public int getCounter() {
		return counter;
	}
	public void setCounter(int counter) {
		this.counter = counter;
	}
	public int getMaxThreads() {
		return maxThreads;
	}
	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

}
