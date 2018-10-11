import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


public class Controller {
	/************************************/
	boolean checkSorted = true;
	boolean printItBefore = false;
	boolean printItAfter = false;
	int seed = 1234;
	int maxWerte = Integer.MAX_VALUE;
	/***********************************/
	
	int[] zahlen;
	int[] sorted;
	int maxThreads;
	final CountDownLatch doneSignal;
	MyThread[] threads;
	CyclicBarrier barrier;
	int counter = 0;
	
	Controller(int amountZahlen) throws InterruptedException, BrokenBarrierException{
		zahlen = new int[amountZahlen];
		sorted = new int[amountZahlen];
		maxThreads = zahlen.length;
		threads = new MyThread[amountZahlen];
		barrier = new CyclicBarrier(maxThreads + 1);
		
	}
	
	public void generateNumbers(int numbers) {
		Random rand = new Random(seed);
		for(int i = 0; i < numbers; ++i) {
			zahlen[i] = rand.nextInt(maxWerte);
		}
	}
	
	public void doit() throws InterruptedException, BrokenBarrierException {
		for(int i = 0; i < maxThreads; ++i) {
			threads[i] = new MyThread(i, zahlen[i], barrier, maxThreads);
		}
		
		for(int i = 0; i < maxThreads-1; ++i) {
			threads[i].setNext(threads[i+1]);
		}
		
		for(int i = 0; i < maxThreads; ++i) {
			threads[i].start();
		}
		
		//durch CountDownLatch ersetzen
		/*while(counter < maxThreads) {
			barrier.await();
			++counter;
			barrier.await();
		}*/
		
		for(int i = 0; i < maxThreads; ++i) {
			sorted[i] = threads[i].getNumber();
		}
	}
	
	public void printIt() {
		for(int i = 0; i < threads.length; ++i) {
			System.out.println("Kern: "+(i+1)+" Nummer: "+threads[i].getNumber());
		}
	}
	
	public String isSorted() {
		Arrays.sort(zahlen);
		for(int i = 0; i < sorted.length; ++i) {
			if(sorted[i] != zahlen[i]) {
				return "Zahl: "+sorted[i]+" nicht sortiert.";
			}
		}
		return "Alle Zahlen sortiert.";
	}
	
	public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
		int maxNumbers = 100;
		Controller c = new Controller(maxNumbers);
		c.generateNumbers(maxNumbers);
		if(c.printItBefore) {
			System.out.println("Vor dem Sortieren");
			c.printIt();
		}
		c.doit();
		if(c.checkSorted) {
			System.out.println(c.isSorted());
		}
		if(c.printItAfter) {
			System.out.println("Nach dem Sortieren");
			c.printIt();
		}
	}
	
}
