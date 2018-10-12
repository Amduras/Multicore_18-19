import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class MyThread extends Thread{

	private MyThread next, prev;
	private Integer myId;
	private int[] numbers;
	private CyclicBarrier barrier;
	private int counter = 0;
	private int maxThreads;
	private final CountDownLatch doneSignal;
	
	public MyThread(Integer name, int[] numbers, CyclicBarrier barrier, int maxThreads, CountDownLatch doneSignal) {
		myId = name;
		this.numbers = numbers;
		this.barrier = barrier;
		this.maxThreads = maxThreads;
		this.doneSignal = doneSignal;
	}
	
	@Override
	public void run() {
		while(counter < maxThreads) {
			Arrays.sort(numbers);
			try {
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			int[] tmp = swap();
			try {
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			numbers = tmp;
			++counter;
			try {
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		doneSignal.countDown();
	}
	
	public int[] swap() {
		if (counter % 2 == myId % 2 && next != null) {
			return sortEven();
		} else if (counter % 2 != myId % 2 && prev != null) {
			return sortUneven();
		}
		return numbers;
	}
	
	int[] sortEven() {
		int[] nextDigits = next.getNumbers();
		int[] sorted = new int[numbers.length];
		int i = 0, j = 0, k = 0;
		if(numbers[numbers.length - 1] > nextDigits[0]) {
			while(k < numbers.length) {
				if(numbers[i] <= nextDigits[j]) {
					sorted[k] = numbers[i];
					++k;
					++i;
				} else if(numbers[i] > nextDigits[j]) {
					sorted[k] = nextDigits[j];
					++k;
					++j;
				}
			}
			return sorted;
		} else {
			return numbers;
		}
	}
	
	int[] sortUneven() {
		int[] prevDigits = prev.getNumbers();
		int[] sorted = new int[numbers.length];
		int i = prevDigits.length - 1, j = numbers.length - 1, k = numbers.length - 1;
		if(numbers[0] < prevDigits[prevDigits.length - 1]) {
			while(k >= 0) {
				if(prevDigits[i] > numbers[j]) {
					sorted[k] = prevDigits[i];
					--k;
					--i;
				} else if(prevDigits[i] <= numbers[j]) {
					sorted[k] = numbers[j];
					--k;
					--j;
				}
			}
			return sorted;
		} else {
			return numbers;
		}
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
	public int[] getNumbers() {
		return numbers;
	}
	public void setNumbers(int[] digits) {
		this.numbers = digits;
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
	public MyThread getPrev() {
		return prev;
	}

	public void setPrev(MyThread prev) {
		this.prev = prev;
	}
	
}
