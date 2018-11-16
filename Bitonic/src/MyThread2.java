import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class MyThread2 extends AbstractMyThread {

	public MyThread2(CyclicBarrier barrier, int Id, int cubeSize) {
		myId = Id;
		this.barrier = barrier;
		setCubeSize(cubeSize);
	}

	//final static int AUF = 0, AB = 1;
	int[] myTmp, newTmp;

	@Override
	public void run() {
		myWait();
		Arrays.sort(numbers);
		myWait();
		myTmp = new int[numbers.length];
		// Presort
		while (counter < cubeSize) {
			int secondCounter = counter;
			while (secondCounter >= 0) {
				final AbstractMyThread otherThread = threadList[myId ^ 1 << secondCounter];
				final int temper = myId % (int) (Math.pow(2, counter + 2));
				if (temper < (int) (Math.pow(2, counter + 1))) {
					if (myId < otherThread.myId) {
						myTmp = mergesortEven(otherThread.numbers);
					} else {
						myTmp = mergesortUneven(otherThread.numbers);
					}
				} else {
					if (myId < otherThread.myId) {
						myTmp = mergesortUneven(otherThread.numbers);
					} else {
						myTmp = mergesortEven(otherThread.numbers);
					}
				}
				myWait();
				newTmp = myTmp;
				myTmp = numbers;
				numbers = newTmp;
				myWait();
				--secondCounter;
			}
			++counter;
		}
		myWait();
		// Sort
		counter = 0;
		while (counter < getCubeSize()) {
			final int currentDim = getCubeSize() - counter;
			myTmp = mergesort(currentDim);
			myWait();
			newTmp = myTmp;
			myTmp = numbers;
			numbers = newTmp;
			++counter;
			myWait();
		}

	}

	private int[] mergesort(int currentDim) {
		final AbstractMyThread otherThread = threadList[myId ^ (1 << currentDim - 1)];
		final int[] otherNumbers = otherThread.getNumbers();
		if (myId > otherThread.myId) {
			return mergesortUneven(otherNumbers);
		} else {
			return mergesortEven(otherNumbers);
		}
	}

	private int[] mergesortUneven(int[] otherNumbers) {
		int[] sorted = new int[numbers.length];
		int i = numbers.length - 1, j = otherNumbers.length - 1, k = numbers.length - 1;
		if (numbers[0] < otherNumbers[otherNumbers.length - 1]) {
			while (k >= 0) {
				if (i < 0) {
					sorted[k--] = otherNumbers[j--];
				} else if (j < 0) {
					sorted[k--] = numbers[i--];
				} else {
					if (otherNumbers[j] > numbers[i]) {
						sorted[k--] = otherNumbers[j--];
					} else if (otherNumbers[j] <= numbers[i]) {
						sorted[k--] = numbers[i--];
					}
				}
			}
			return sorted;
		} else {
			return numbers;
		}
	}

	private int[] mergesortEven(int[] otherNumbers) {
		final int[] sorted = new int[numbers.length];
		int i = 0, j = 0, k = 0;
		if (numbers[numbers.length - 1] > otherNumbers[0]) {
			while (k < numbers.length) {
				if (i >= numbers.length) {
					sorted[k++] = otherNumbers[j++];
				} else if (j >= otherNumbers.length) {
					sorted[k++] = numbers[i++];
				} else {
					if (numbers[i] <= otherNumbers[j]) {
						sorted[k++] = numbers[i++];
					} else if (numbers[i] > otherNumbers[j]) {
						sorted[k++] = otherNumbers[j++];
					}
				}
			}
			return sorted;
		} else {
			return numbers;
		}
	}

	private void myWait() {
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
	}

	private void printMessage(String message) {
		if (myId == 3) {
			System.out.println(message);
		}
	}
}
