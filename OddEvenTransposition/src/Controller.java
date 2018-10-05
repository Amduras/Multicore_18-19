import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


public class Controller {
	int[] test = {4,3,2,1};
	int maxThreads;
	MyThread[] threads;
	CyclicBarrier barrier;
	
	Controller(int amountZahlen) throws InterruptedException, BrokenBarrierException{
		maxThreads = test.length;
		threads = new MyThread[amountZahlen];
		barrier = new CyclicBarrier(amountZahlen+1);
		doit();
	}
	
	public void doit() throws InterruptedException, BrokenBarrierException {
		for(int i = 0; i < maxThreads; ++i) {
			threads[i] = new MyThread(i, test[i], barrier, maxThreads);
		}
		System.out.println("Vor dem Sortieren:");
		printIt();
		
		for(int i = 0; i < maxThreads-1; ++i) {
			threads[i].setNext(threads[i+1]);
		}
		
		for(int i = 0; i < maxThreads; ++i) {
			threads[i].start();
		}
		barrier.await();
		System.out.println("Nach dem Sortieren:");
		printIt();
	}
	
	public void printIt() {
		for(int i = 0; i < threads.length; ++i) {
			System.out.println("Kern: "+(i+1)+" Nummer: "+threads[i].getNumber());
		}
	}
	
	public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
		Controller c = new Controller(4);
	}
	
}
