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
		int[] tmp = new int[numbers.length];
		//Presort
		while(stages < cubeSize-1) {
			int steps = stages;
			while(steps >= 0) {
				final MyThread compareThread = threadlist[id ^ 1 << steps];
				final int temper = id % (int) (Math.pow(2, stages + 2));
				if (temper < (int) (Math.pow(2, stages + 1))) {
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
				waitAll();
				numbers = tmp;
				waitAll();
				--steps;
			}
			++stages;
		}
		doneSignal.countDown();
	}
	
	
//	private int[] sortUneven(int[] otherNumbers) {
//		int[] sorted = new int[numbers.length];
//		int i = numbers.length - 1, j = otherNumbers.length - 1, k = numbers.length - 1;
//		if (numbers[0] < otherNumbers[otherNumbers.length - 1]) {
//			while (k >= 0) {
//				if (i < 0) {
//					sorted[k--] = otherNumbers[j--];
//				} else if (j < 0) {
//					sorted[k--] = numbers[i--];
//				} else {
//					if (otherNumbers[j] > numbers[i]) {
//						sorted[k--] = otherNumbers[j--];
//					} else if (otherNumbers[j] <= numbers[i]) {
//						sorted[k--] = numbers[i--];
//					}
//				}
//			}
//			return sorted;
//		} else {
//			return numbers;
//		}
//	}
//
//	private int[] sortEven(int[] otherNumbers) {
//		final int[] sorted = new int[numbers.length];
//		int i = 0, j = 0, k = 0;
//		if (numbers[numbers.length - 1] > otherNumbers[0]) {
//			while (k < numbers.length) {
//				if (i >= numbers.length) {
//					sorted[k++] = otherNumbers[j++];
//				} else if (j >= otherNumbers.length) {
//					sorted[k++] = numbers[i++];
//				} else {
//					if (numbers[i] <= otherNumbers[j]) {
//						sorted[k++] = numbers[i++];
//					} else if (numbers[i] > otherNumbers[j]) {
//						sorted[k++] = otherNumbers[j++];
//					}
//				}
//			}
//			return sorted;
//		} else {
//			return numbers;
//		}
//	}
	
	private int[] sortEven(int[] otherNumbers) {
		int[] sorted = new int[numbers.length];
		int i = 0, j = 0, k = 0;
		if(numbers[numbers.length - 1] > otherNumbers[0]) {
			while(k < numbers.length) {
				if(numbers[i] <= otherNumbers[j]) {
					sorted[k] = numbers[i];
					++k;
					++i;
				} else if(numbers[i] > otherNumbers[j]) {
					sorted[k] = otherNumbers[j];
					++k;
					++j;
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
				if(otherNumbers[i] > numbers[j]) {
					sorted[k] = otherNumbers[i];
					--k;
					--i;
				} else if(otherNumbers[i] <= numbers[j]) {
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
