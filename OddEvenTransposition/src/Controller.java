import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
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
	boolean writeIt = true;
	int seed;
	int maxWerte = Integer.MAX_VALUE;
	int maxCores;
	int startCores;
	int coreNumbers;
	int cores;
	int maxRuns;
	int runs;
	private int startSort = ARRAYSORT;
	/***********************************/

	int amountZahlen;
	long startPar;
	long stopPar;
	long gcStart;
	long gcStop;
	long gcTime;
	long start;
	long stop;
	int[][] zahlen;
	int[] sorted;
	int[] zahlenSorted;
	int[] zahlenPar;
	double time;
	CountDownLatch doneSignal;
	MyThread[] threads;
	CyclicBarrier barrier;
	DecimalFormat nf;
	
	Controller(int amountZahlen, int maxCores, int maxRuns, int startCores, int seed)
			throws InterruptedException, BrokenBarrierException {
		this.maxCores = maxCores;
		this.maxRuns = maxRuns;
		this.startCores = startCores;
		time = 0;
		this.amountZahlen = amountZahlen;
		this.seed = seed;
		nf = new DecimalFormat();
	}

	public void generateNumbers(int numbers) {
		int x = 0;
		Random rand = new Random(seed);
		for (int i = 0; i < numbers; ++i) {
			for (int j = 0; j < coreNumbers; ++j) {
				zahlen[i][j] = rand.nextInt();
				zahlenSorted[x] = zahlen[i][j];
				zahlenPar[x] = zahlen[i][j];
				++x;
			}
		}
	}

	public void doit() throws InterruptedException, BrokenBarrierException {
		for (startSort = ARRAYSORT; startSort <= BUBBLESORT; ++startSort) {
			if (startSort == BUBBLESORT) {
				if(amountZahlen >= 100000000) {
					amountZahlen /= 10000;
				} else if(amountZahlen > 1000){
					amountZahlen /= 1000;
				}
			}
			zahlenSorted = new int[amountZahlen];
			zahlenPar = new int[amountZahlen];
			sorted = new int[amountZahlen];
			
			for (cores = startCores; cores <= maxCores; ++cores) {
				
				if(cores > 10 && cores <= 20) {
					++cores;
				}
				
				if(cores > 20) {
					cores+=4;
				}
				
				if (amountZahlen % cores == 0) {
					gcTime = 0;
					System.out.println("Kerne: " + cores);
					System.out.println("Zahlenmenge: " + nf.format(amountZahlen));
					System.out.print("Run: ");
					threads = new MyThread[cores];
					barrier = new CyclicBarrier(cores);
					time = 0;
					
					for (runs = 0; runs < maxRuns; ++runs) {
						System.out.print("#");
						if ((runs + 1) % 5 == 0 && runs != 0) {
							System.out.print("  ");
						}
						
						coreNumbers = amountZahlen / cores;
						zahlen = new int[cores][coreNumbers];
						doneSignal = new CountDownLatch(cores);
						generateNumbers(cores);
						gcStart = System.nanoTime();
						System.gc();
						gcStop = System.nanoTime();
						gcTime+=((gcStop - gcStart)) / 1000000.0;
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

						if(time > (stop-start) / 1000000.0 || time == 0) {
							time = (stop-start) / 1000000.0;
						}
						
						for (int i = 0; i < cores; ++i) {
							System.arraycopy(threads[i].getNumbers(), 0, sorted, (i * coreNumbers),
									threads[i].getNumbers().length);
						}
					}
					
					if(writeIt) {
						writeIt();
					}
					
					if (checkSorted) {
						System.out.println(isSorted());
					}

					System.out.println("GC Time: " +gcTime);
					System.out.println("Beste Zeit: " + time);
					System.out.println("Java Parralelsort: " + ((stopPar - startPar) / 1000000.0));
					System.out.println("Seq. Algo: " + (startSort == ARRAYSORT ? "ArraySort" : "BubbleSort"));
					System.out.println(
							"----------------------------------------------------------------------------------");
				}
			}
		}
	}
	
	private void writeIt() {
		try {
			FileWriter writer = new FileWriter("Auswertung.txt", true);
			if (cores == 1) {
				writer.write("Zahlenmenge: " + nf.format((amountZahlen)));
				writer.write(System.lineSeparator());
				String str = (startSort == 1 ? "ARRAYSORT" : "BUBBLESORT");
				writer.write(str);
				writer.write(System.lineSeparator());
			}
			writer.write("Kerne: " + (cores) + "\t");
			writer.write(Double.toString(time) + " ms");
			writer.write(System.lineSeparator());
			writer.close();
			System.out.println("\nDaten gespeichert.");
		} catch (IOException e) {
			System.out.println("\nSpeichern der Daten gescheitert.");
			e.printStackTrace();
		}
	}
	
	public void printIt() {
		int kerne = 1;
		System.out.print("Kern: " + (kerne++) + " Nummern: ");
		for (int i = 0; i < sorted.length; ++i) {
			if (i % coreNumbers == 0) {
				System.out.println();
				System.out.print("Kern: " + (kerne++) + " Nummern: ");
			}
			System.out.print(sorted[i] + "\t");
		}
	}

	public String isSorted() {
		startPar = System.nanoTime();
		Arrays.parallelSort(zahlenPar);
		stopPar = System.nanoTime();
		if (sorted.length == amountZahlen) {
			for (int i = 0; i < sorted.length; ++i) {
				if (sorted[i] != zahlenPar[i]) {
					return "\nKerne: " + cores + " Zahlen: " + sorted[i] + " nicht Sortiert. Erwartete Zahl: "
							+ zahlenSorted[i] + " Position: " + i;
				}
			}
			return "Alle Zahlen sortiert.";
		} else {
			return "Zahlen verloren.";
		}
	}

	public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
		// args[0] = amountZahlen
		// args[1] = maxCores
		// args[2] = maxRuns
		// args[3] = startCores;
		// args[4] = seed
		
		//writer writen, 1-10 cores: 1er schritte, 10-20: 2er schritte: ab 20: 5er schritte 
		
		Controller c = new Controller(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]),
				Integer.parseInt(args[3]), Integer.parseInt(args[4]));
		c.doit();
	}

}
