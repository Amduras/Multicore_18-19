import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class MyThread extends Thread{
	
	int id;
	int[] numbers;
	int cubeSize;
	int stages = 0;
	MyThread[] threadlist;
	CyclicBarrier barrier;
	CountDownLatch doneSignal;
	
	MyThread(int id, CyclicBarrier barrier, CountDownLatch doneSignal, MyThread[] list, int cubeSize){
		this.id = id;
		this.barrier = barrier;
		this.doneSignal = doneSignal;
		threadlist = list;
		this.cubeSize = cubeSize;
	}
	
	@Override
	public void run() {
		Arrays.sort(numbers);
		waitAll();
		//Presort
		while(stages < cubeSize) {
			int steps = stages;
			while(stages >= 0) {
				final MyThread compareThread = threadlist[id ^ 1 << stages];
			}
		}
	}
	
	public void waitAll() {
		try {
			barrier.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setNumbers(int[] numbers) {
		this.numbers = numbers;
	}
	
	public int[] getNumbers() {
		return numbers;
	}
}
