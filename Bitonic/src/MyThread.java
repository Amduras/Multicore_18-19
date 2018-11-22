import java.util.Arrays;
import java.util.Collection;
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
	int counter = 0;
	
	MyThread(int id, CyclicBarrier barrier, CountDownLatch doneSignal, MyThread[] list, int cubeSize){
		this.id = id;
		this.barrier = barrier;
		this.doneSignal = doneSignal;
		threadlist = list;
		this.cubeSize = cubeSize;
	}
	
	@Override
	public void run() {
		//Presort der lokalenlisten
		Arrays.sort(numbers);
		waitAll();
		int[] tmp = new int[numbers.length];
		//Zahlenfolgen Bitonisch machen
		while(stages < cubeSize-1) {
			int steps = stages;
			while(steps >= 0) {
				final MyThread compareThread = threadlist[id ^ 1 << steps];
				final int temp = id % (int) (Math.pow(2, stages + 2));
				if (temp < (int) (Math.pow(2, stages + 1))) {
					if (id < compareThread.id) {
						tmp = sortEven(compareThread.numbers);
					} else {
						tmp = sortUneven(compareThread.numbers);
					}
				} else {
					if (id < compareThread.id) {
						tmp = sortUneven(compareThread.numbers);
					} else {
						tmp = sortEven(compareThread.numbers);
					}
				}
				++counter;
				waitAll();
				numbers = tmp;
				waitAll();
				--steps;
			}
			++stages;
		}
		waitAll();
		System.out.println("Thread: "+id+" Schritte: "+counter);
		waitAll();
		//Sortieren der listen
		doneSignal.countDown();
	}

	private int[] sortEven(int[] otherNumbers) {
		int[] sorted = new int[numbers.length];
		int i = 0, j = 0, k = 0;
		if(numbers[numbers.length - 1] > otherNumbers[0]) {
			while(k < numbers.length) {
				if (i >= numbers.length) {
					sorted[k++] = otherNumbers[j++];
				} else if (j >= otherNumbers.length) {
					sorted[k++] = numbers[i++];
				} else {
					if(numbers[i] <= otherNumbers[j]) {
						sorted[k++] = numbers[i++];
					} else if(numbers[i] > otherNumbers[j]) {
						sorted[k++] = otherNumbers[j++];
					}
				}
			}
			return sorted;
		} else {
			return numbers;
		}
	}
	
	private int[] sortUneven(int[] otherNumbers) {
		int[] sorted = new int[numbers.length];
		int i = otherNumbers.length - 1, j = numbers.length - 1, k = numbers.length - 1;
		if(numbers[0] < otherNumbers[otherNumbers.length - 1]) {
			while(k >= 0) {
				if( i < 0) {
					sorted[k--] = otherNumbers[j--];
				} else if(j < 0) {
					sorted[k--] = numbers[i--];
				} else {
					if(otherNumbers[i] > numbers[j]) {
						sorted[k--] = otherNumbers[i--];
					} else if(otherNumbers[i] <= numbers[j]) {
						sorted[k--] = numbers[j--];
					}
				}
			}
			return sorted;
		} else {
			return numbers;
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
