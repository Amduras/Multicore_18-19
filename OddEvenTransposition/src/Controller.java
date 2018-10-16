import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;


public class Controller {
	
	@SuppressWarnings("unused")
	private final Integer ARRAYSORT = 1, BUBBLESORT = 2;
	/************************************/
	boolean checkSorted = true;
	boolean printItBefore = false;
	boolean printItAfter = false;
	int seed = 1234;
	int maxWerte = Integer.MAX_VALUE;
	int cores;
	int coreNumbers;
	private int startSort = ARRAYSORT;
	/***********************************/
	
	long start;
	long stop;
	int[][] zahlen;
	//int[][] zahlen = {{9,8},{7,6},{5,4},{3,2},{1,0}};
	int[][] sorted;
	int[] zahlenSorted;
	final CountDownLatch doneSignal;
	MyThread[] threads;
	CyclicBarrier barrier;
	
	Controller(int amountZahlen, int amountCores) throws InterruptedException, BrokenBarrierException{
		cores = amountCores;
		if(startSort == BUBBLESORT) {
			amountZahlen/=100;
		}
		
		coreNumbers = amountZahlen/cores;
		zahlenSorted = new int[amountZahlen];
		zahlen = new int[cores][coreNumbers];
		sorted = new int[cores][coreNumbers];
		doneSignal = new CountDownLatch(cores);
		threads = new MyThread[cores];
		barrier = new CyclicBarrier(cores);
		
	}
	
	public void generateNumbers(int numbers) {
		int x = 0;
		Random rand = new Random(seed);
		for(int i = 0; i < numbers; ++i) {
			for(int j = 0; j < coreNumbers; ++j) {
				zahlen[i][j] = rand.nextInt(maxWerte);
				zahlenSorted[x] = zahlen[i][j];
				++x;
			}
		}
	}
	
	public void doit() throws InterruptedException, BrokenBarrierException {
		for(int i = 0; i < cores; ++i) {
				threads[i] = new MyThread(i, zahlen[i], barrier, cores, doneSignal, startSort);
		}
		
		for(int i = 0; i < cores-1; ++i) {
			threads[i].setNext(threads[i+1]);
		}
		
		for(int i = 1; i < cores; ++i) {
			threads[i].setPrev(threads[i-1]);
		}
		
		for(int i = 0; i < cores; ++i) {
			threads[i].start();
		}
		
		doneSignal.await();
		
		for(int i = 0; i < cores; ++i) {
			sorted[i] = threads[i].getNumbers();
		}
	}
	
	public void printIt() {
		for(int i = 0; i < threads.length; ++i) {
				System.out.print("Kern: "+(i+1)+" Nummern: ");
			for(int j = 0; j < sorted[i].length; ++j) {
				System.out.print(sorted[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	public String isSorted() {
		int x = 0;
		Arrays.sort(zahlenSorted);
		for(int i = 0; i < threads.length; ++i) {
			for(int j = 0; j < sorted[i].length; ++j) {
				if(sorted[i][j] != zahlenSorted[x]) {
					return "Zahl: "+sorted[i][j]+" nicht sortiert. Zahl: "+zahlenSorted[x];
				}
				++x;
			}
		}
		return "Alle Zahlen sortiert.";
	}
	
	public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
		int maxNumbers = 10000000;
		int maxCores = 4;
		
		if(maxNumbers % maxCores != 0) {
			System.out.println("Zahlen lassen sich nicht auffteilen");
		} else {
			Controller c = new Controller(maxNumbers, maxCores);
			c.generateNumbers(maxCores);
			if(c.printItBefore) {
				System.out.println("Vor dem Sortieren");
				c.printIt();
			}
			c.start = System.nanoTime();
			c.doit();
			c.stop = System.nanoTime();
			System.out.println("Zeit zum sortieren mit "+maxCores+" Kernen: "+((c.stop - c.start) / 1000000.0+"ms"));
			if(c.checkSorted) {
				System.out.println(c.isSorted());
			}
			if(c.printItAfter) {
				System.out.println("Nach dem Sortieren");
				c.printIt();
			}
		}
	}
	
}
