import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class MyThread2 extends Thread {

	private int[] digits;
	private MyThread2 next, prev;
	private int counter = -1, maxThreads;
	Integer myId;
	private CyclicBarrier barrier;

	private Integer startSorter = 0;
	@SuppressWarnings("unused")
	private final static Integer ARRAYSORT = 1, BUBBLESORT = 2;

	public MyThread2(int[] digits, int maxThreads, Integer name, CyclicBarrier barrier, Integer startSorter) {
		this.digits = new int[digits.length];
		System.arraycopy(digits, 0, this.digits, 0, digits.length);
		this.maxThreads = maxThreads;
		this.barrier = barrier;
		this.myId = name;
		this.startSorter = startSorter;
	}

	@Override
	public void run() {
		// Vorsortierung
		if (startSorter == ARRAYSORT) {
			Arrays.sort(digits);
		} else {
			bubblesort();
		}
		counter = 0;
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e1) {
			e1.printStackTrace();
		}

		// Start der Sortierung
		while (counter < maxThreads) {
			int[] tmp = mergesort();
			try {
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				System.out.println("Prozessor " + myId + " erlitt einen kritischen Fehlschlag während des wartens!");
				e.printStackTrace();
			}
			digits = tmp;
			++counter;
			try {
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				System.out.println("Prozessor " + myId + " erlitt einen kritischen Fehlschlag während des wartens!");
				e.printStackTrace();
			}
		}
	}

	private int[] mergesort() {
		if (counter % 2 == myId % 2 && next != null) {
			return mergesortEven();
		} else if (counter % 2 != myId % 2 && prev != null) {
			return mergesortUneven();
		}
		return digits;

	}

	private int[] mergesortUneven() {
		final int[] prevDigits = prev.getDigits();
		int[] sorted = new int[digits.length];
		int i = digits.length - 1, j = prevDigits.length - 1, k = digits.length - 1;
		if (digits[0] < prevDigits[prevDigits.length - 1]) {
			while (k >= 0) {
				if (i < 0) {
					sorted[k] = prevDigits[j];
					--j;
					--k;
				} else if (j < 0) {
					sorted[k] = digits[i];
					--i;
					--k;
				} else {
					if (prevDigits[j] > digits[i]) {
						sorted[k] = prevDigits[j];
						--j;
						--k;
					} else if (prevDigits[j] <= digits[i]) {
						sorted[k] = digits[i];
						--i;
						--k;
					}
				}
			}
			return sorted;
		} else {
			return digits;
		}
	}

	private int[] mergesortEven() {
		final int[] nextDigits = next.getDigits();
		int[] sorted = new int[digits.length];

		int i = 0, j = 0, k = 0;
		if (digits[digits.length - 1] > nextDigits[0]) {
			while (k < digits.length) {
				if (i >= digits.length) {
					sorted[k] = nextDigits[j];
					++j;
					++k;
				} else if (j >= nextDigits.length) {
					sorted[k] = digits[i];
					++i;
					++k;
				} else {
					if (digits[i] <= nextDigits[j]) {
						sorted[k] = digits[i];
						++i;
						++k;
					} else if (digits[i] > nextDigits[j]) {
						sorted[k] = nextDigits[j];
						++j;
						++k;
					}
				}
			}
			return sorted;
		} else {
			return digits;
		}
	}

	private void bubblesort() {

		for (int i = 1; i < digits.length; ++i) {
			for (int j = 0; j < digits.length - i; ++j) {
				if (digits[j] > digits[j + 1]) {
					final int tmp = digits[j + 1];
					digits[j + 1] = digits[j];
					digits[j] = tmp;

				}
			}
		}
	}

	public int[] getDigits() {
		return digits;
	}

	public void setDigits(int[] newDigits) {
		this.digits = new int[newDigits.length];
		System.arraycopy(newDigits, 0, this.digits, 0, newDigits.length);
	}

	public MyThread2 getNext() {
		return next;
	}

	public void setNext(MyThread2 next) {
		this.next = next;
	}

	public MyThread2 getPrev() {
		return prev;
	}

	public void setPrev(MyThread2 prev) {
		this.prev = prev;
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

	public Integer getMyId() {
		return myId;
	}

	public void setMyID(Integer myID) {
		this.myId = myID;
	}

	public CyclicBarrier getBarrier() {
		return barrier;
	}

	public void setBarrier(CyclicBarrier barrier) {
		this.barrier = barrier;
	}
}
