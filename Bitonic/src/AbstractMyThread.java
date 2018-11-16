import java.util.concurrent.CyclicBarrier;

public abstract class AbstractMyThread extends Thread {

	static AbstractMyThread[] threadList;
	protected int[] numbers;
	protected int myId;
	protected int counter = 0;
	protected int cubeSize;
	protected CyclicBarrier barrier;
	//protected static int AUF = 0, AB = 1;

	public void setThreadList(AbstractMyThread[] list) {
		threadList = list;
	}

	public int getMyId() {
		return myId;
	}

	public void setMyId(int myId) {
		this.myId = myId;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public int[] getNumbers() {
		return numbers;
	}

	public void setNumbers(int[] numbers) {
		this.numbers = numbers;
	}

	public CyclicBarrier getBarrier() {
		return barrier;
	}

	public void setBarrier(CyclicBarrier barrier) {
		this.barrier = barrier;
	}

	public int getCubeSize() {
		return cubeSize;
	}

	public void setCubeSize(int cubeSize) {
		this.cubeSize = cubeSize;
	}

}
