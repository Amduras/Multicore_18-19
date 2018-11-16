import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class MyThread {
	
	int id;
	int numbers;
	CyclicBarrier barrier;
	CountDownLatch doneSignal;
	
	MyThread(int id, CyclicBarrier barrier, CountDownLatch doneSignal){
		this.id = id;
		this.barrier = barrier;
		this.doneSignal = doneSignal;
	}
	
	public void setNumbers(int numbers) {
		this.numbers = numbers;
	}
	
	public int getNumbers() {
		return numbers;
	}
}
