import java.util.concurrent.CyclicBarrier;

public class MyThread extends Thread{
	
	private MyThread next;
	private Integer myId;
	private int number;
	private CyclicBarrier barrier;
	private int counter = 0;
	private int maxThreads;
	
	public MyThread(int name, int number, CyclicBarrier barrier, int maxThreads) {
		myId = name;
		this.number = number;
		this.barrier = barrier;
		this.maxThreads = maxThreads;
	}
	@Override
	public void run() {
		while(counter < maxThreads) {
			if(counter % 2 == myId % 2 && next != null) {
				if(number > next.getNumber()) {
					swap(number, next.getNumber());
				}
			}
			System.out.println(getNumber());
		}
	}
	
	public void swap(int myNumber, int nextNumber) {
		int tmp = myNumber;
		myNumber = nextNumber;
		nextNumber = tmp;
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
}