import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import javax.swing.JFrame;


public class Controller {
	
	@SuppressWarnings("unused")
	private final Integer ARRAYSORT = 1, BUBBLESORT = 2;
	/************************************/
	boolean checkSorted = true;
	boolean printItBefore = false;
	boolean printItAfter = true;
	int seed = 1234;
	//int maxWerte = Integer.MAX_VALUE;
	int maxWerte = 100;
	int maxCores;
	int coreNumbers;
	int cores;
	int maxRuns;
	int runs;
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
	double[][] time;
	CountDownLatch doneSignal;
	MyThread[] threads;
	CyclicBarrier barrier;
	
	Controller(int amountZahlen, int maxCores, int maxRuns) throws InterruptedException, BrokenBarrierException{
		this.maxCores = maxCores;
		this.maxRuns = maxRuns;
		if(startSort == BUBBLESORT) {
			amountZahlen/=1000;
		}
		time = new double[maxCores][1];
		this.amountZahlen = amountZahlen;
		zahlenSorted = new int[amountZahlen];
		zahlenPar = new int[amountZahlen];
		sorted = new int[amountZahlen];
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
		for(cores = 1; cores <= maxCores; ++cores) {
			System.out.println("Kerne: " + cores);
			System.out.print("Run: ");
			for(runs = 0; runs < maxRuns; ++runs) {
				System.out.print("#");
				if((runs + 1) % 5 == 0 && runs != 0) {
					System.out.print("  ");
				}
				System.gc();
				coreNumbers = amountZahlen / cores;
				zahlen = new int[cores][coreNumbers];
				doneSignal = new CountDownLatch(cores);
				threads = new MyThread[cores];
				barrier = new CyclicBarrier(cores);

				generateNumbers(cores);
				
//				int rest = zahlen[cores-1].length % coreNumbers;
//				int currentRest = 0;
//				for (int i = 0; i < threads.length; ++i) {
//					final int[] digits;
//					if (rest > 0) {
//						digits = new int[coreNumbers];
//						--rest;
//					} else {
//						digits = new int[coreNumbers];
//					}
//					
//					System.arraycopy(zahlen[i], 0, digits, 0, digits.length);
//					if (digits.length % coreNumbers == 1) {
//						++currentRest;
//					}
//					threads[i] = new MyThread(i, digits, barrier, cores, doneSignal, startSort);
//				}
				
				for (int i = 0; i < cores; ++i) {
					threads[i] = new MyThread(i, zahlen[i], barrier, cores, doneSignal, startSort);
				}

				for (int i = 0; i < cores - 1; ++i) {
					threads[i].setNext(threads[i + 1]);
				}

				for (int i = 1; i < cores; ++i) {
					threads[i].setPrev(threads[i - 1]);
				}

				start = System.nanoTime();
				for (int i = 0; i < cores; ++i) {
					threads[i].start();
				}

				doneSignal.await();
				stop = System.nanoTime();
				if(time[cores-1][0] < (stop - start) / 1000000.0 || time[cores-1][0] == 0) {
					time[cores-1][0] = (stop - start) / 1000000.0;
				}
				
//				int reste = 0;
//				for (int i = 0; i < cores; ++i) {
//					System.arraycopy(threads[i].getNumbers(), 0, sorted, (i * coreNumbers) + reste,
//							threads[i].getNumbers().length);
//					if (threads[i].getNumbers().length % coreNumbers == 1) {
//						++reste;
//					}
//				}
				
				for (int i = 0; i < cores; ++i) {
					System.arraycopy(threads[i].getNumbers(), 0, sorted, (i * coreNumbers),
							threads[i].getNumbers().length);
				}

//				System.out.println("Zeit zum sortieren von " + amountZahlen + " Zahlen mit " + threads.length
//						+ " Kernen: " + ((stop - start) / 1000000.0 + "ms"));
				
				// System.out.println("Zeit zum sortieren von "+amountZahlen+" Zahlen mit
				// Arrays.sort: "+((stopSeq - startSeq) / 1000000.0+"ms"));
//				System.out.println("Zeit zum sortieren von " + amountZahlen + " Zahlen mit Arrays.parralelSort: "
//						+ ((stopPar - startPar) / 1000000.0 + "ms"));
			}
			
			if (checkSorted) {
				System.out.println(isSorted());
			}
			
			System.out.println("Beste Zeit: "+time[cores-1][0]);
			if(printItAfter) {
				printIt();
			}
			System.out.println("----------------------------------------------------------------------------------");
		}
	}
	
	public void printIt() {
		int x = 0;
		for(int i = 0; i < threads.length; ++i) {
			//if(x == coreNumbers) {
			System.out.print("Kern: "+(i+1)+" Nummern: ");
			for(int j = 0; j < threads[i].getNumbers().length; ++j) {
				//System.out.print(threads[i].getNumbers()[j]+" ");
				System.out.print((x++)+" ");
			}
			System.out.println();
		}
	}
	
	public String isSorted() {
		/*startSeq = System.nanoTime();
		Arrays.sort(zahlenSorted);
		stopSeq = System.nanoTime();*/
		startPar = System.nanoTime();
		Arrays.parallelSort(zahlenPar);
		stopPar = System.nanoTime();
		if(sorted.length == amountZahlen) {
			for(int i  = 0; i < sorted.length; ++i) {
				if(sorted[i] != zahlenPar[i]) {
					return "\nKerne: "+cores+" Zahlen: "+sorted[i]+" nicht Sortiert. Erwartete Zahl: "+zahlenSorted[i]+"\n Position "+i;
				}
			}
			return "\nAlle Zahlen sortiert";
		} else {
			return "\nZahlen verloren";
		}
	}
	public static void main(String[] args) throws InterruptedException, BrokenBarrierException { 
//		args[0] = amountZahlen
//		args[1] = maxCores
//		args[2] = maxRuns
		Controller c = new Controller(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		c.doit();
	}
	
}
