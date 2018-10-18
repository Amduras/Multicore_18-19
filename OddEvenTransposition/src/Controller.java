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
	
	int amountZahlen;
	long startPar;
	long stopPar;
	long startSeq;
	long stopSeq;
	long start;
	long stop;
	int[][] zahlen;
	int[] sorted;
	int[] zahlenSorted;
	int[] zahlenPar;
	final CountDownLatch doneSignal;
	MyThread[] threads;
	CyclicBarrier barrier;
	
	Controller(int amountZahlen, int amountCores) throws InterruptedException, BrokenBarrierException{
		cores = amountCores;
		if(startSort == BUBBLESORT) {
			amountZahlen/=100;
		}
		this.amountZahlen = amountZahlen;
		coreNumbers = amountZahlen/cores;
		zahlenSorted = new int[amountZahlen];
		zahlenPar = new int[amountZahlen];
		zahlen = new int[cores][coreNumbers];
		sorted = new int[amountZahlen];
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
				zahlenPar[x] = zahlen[i][j];
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
		start = System.nanoTime();
		for(int i = 0; i < cores; ++i) {
			threads[i].start();
		}
		
		doneSignal.await();
		stop = System.nanoTime();
		for(int i = 0; i < cores; ++i) {
			//sorted[i] = threads[i].getNumbers();
			System.arraycopy(threads[i].getNumbers(), 0, sorted, (i * coreNumbers), threads[i].getNumbers().length);
		}
	}
	
	public void printIt() {
		/*for(int i = 0; i < threads.length; ++i) {
				System.out.print("Kern: "+(i+1)+" Nummern: ");
			for(int j = 0; j < sorted[i].length; ++j) {
				System.out.print(sorted[i][j]+" ");
			}
			System.out.println();
		}*/
		int x = 0;
		int kerne = 1;
		System.out.print("Kern: "+(kerne++)+" Nummern: ");
		for(int i = 0; i < sorted.length; ++i) {
			if(x == coreNumbers) {
				System.out.println();
				System.out.print("Kern: "+(kerne++)+" Nummern: ");
				x = 0;
			}
			System.out.print(sorted[i]+"\t");
			++x;
		}
	}
	
	public String isSorted() {
		/*int x = 0;
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
		*/
		startSeq = System.nanoTime();
		Arrays.sort(zahlenSorted);
		stopSeq = System.nanoTime();
		startPar = System.nanoTime();
		Arrays.parallelSort(zahlenPar);
		stopPar = System.nanoTime();
		if(sorted.length == amountZahlen) {
			for(int i  = 0; i < sorted.length; ++i) {
				if(sorted[i] != zahlenSorted[i]) {
					return "Zahlen: "+sorted[i]+" nicht Sortiert. Erwartete Zahl: "+zahlenSorted[i];
				}
			}
			return "Alle Zahlen sortiert";
		} else {
			return "Zahlen verloren";
		}
	}
	public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
		int maxNumbers = 40000000;
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
			//c.start = System.nanoTime();
			c.doit();
			//c.stop = System.nanoTime();
			System.out.println("Zeit zum sortieren von "+maxNumbers+" Zahlen mit "+maxCores+" Kernen: "+((c.stop - c.start) / 1000000.0+"ms"));			
			if(c.checkSorted) {
				System.out.println(c.isSorted());
			}
			System.out.println("Zeit zum sortieren von "+maxNumbers+" Zahlen mit Arrays.sort: "+((c.stopSeq - c.startSeq) / 1000000.0+"ms"));
			System.out.println("Zeit zum sortieren von "+maxNumbers+" Zahlen mit Arrays.parralelSort: "+((c.stopPar - c.startPar) / 1000000.0+"ms"));
			if(c.printItAfter) {
				System.out.println("Nach dem Sortieren");
				c.printIt();
			}
		}
	}
	
}
