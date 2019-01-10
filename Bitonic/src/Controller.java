import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class Controller {
	
//	########Config###########
	final int amountZahlen;
	final int cubeSizeMax;
	final int cubeSizeStart;
	final int cubeSizeInc;
	int currentCubeSize = 0;
	final int seed;
	final int maxRuns;
	final boolean write = true;
//	#########################
	
	int min = Integer.MAX_VALUE;
	int bestCube;
	int[] zahlen;
	int[] sorted;
	int[] controll;
	MyThread[] threads;
	CyclicBarrier barrier;
	CountDownLatch doneSignal;
	int coreNumbers;
	long start;
	long end;
	
	Controller(int potenz, int cubeSizeMax, int cubeSizeStart, int cubeSizeInc, int maxRuns, int seed){
		this.amountZahlen = (int) Math.pow(2,potenz);
		this.cubeSizeMax = cubeSizeMax;
		this.cubeSizeStart = cubeSizeStart;
		this.cubeSizeInc = cubeSizeInc;
		this.maxRuns = maxRuns;
		this.seed = seed;
		zahlen = new int[amountZahlen];
		sorted = new int[amountZahlen];
		controll = new int[amountZahlen];
		threads = new MyThread[(int) Math.pow(2, currentCubeSize)];
		barrier = new CyclicBarrier(threads.length);
		doneSignal = new CountDownLatch(threads.length);
	}
	
	public void doit() {
		currentCubeSize = cubeSizeStart;
		for(;currentCubeSize <= cubeSizeMax; currentCubeSize += cubeSizeInc) {
			coreNumbers = amountZahlen / (int) (Math.pow(2, currentCubeSize));
			System.out.println("Cores 2^"+currentCubeSize);
			min = Integer.MAX_VALUE;
			for(int runs = 0; runs < maxRuns; ++runs) {
				System.gc();
				System.out.print("Run: ");
				for(int i = 0; i <= runs; ++i) {
					System.out.print("#");
					if(i+1 % 5 == 0) {
						System.out.print(" ");
					}
				}
				System.out.println();
				threads = new MyThread[(int) Math.pow(2, currentCubeSize)];
				barrier = new CyclicBarrier(threads.length);
				doneSignal = new CountDownLatch(threads.length);
				generateNumbers(amountZahlen);
				System.arraycopy(zahlen, 0, controll, 0, zahlen.length);
				Arrays.parallelSort(controll);
				
				
				for(int i = 0; i < threads.length; ++i) {
					threads[i] = new MyThread(i, barrier, doneSignal, threads, currentCubeSize);
					final int[] tmp = new int[coreNumbers];
					System.arraycopy(zahlen, i * coreNumbers, tmp, 0, coreNumbers);
					threads[i].setNumbers(tmp);
				}
				
				start = System.nanoTime();
				for(int i = 0; i < threads.length; ++i) {
					threads[i].start();
				}
				
				try {
					doneSignal.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				end = System.nanoTime();
				
				if(min > (end-start)/1000000) {
					min = (int)((end-start)/1000000);
					bestCube = currentCubeSize;
				}
				
				for (int i = 0; i < threads.length; ++i) {
					System.arraycopy(threads[i].getNumbers(), 0, sorted, (i * coreNumbers),
							threads[i].getNumbers().length);
				}
				
				System.out.println(checkSorted(sorted, controll));
				System.out.println("Benoetigte Zeit: "+((end-start)/1000000));
				
			}
			if(write) {
				writeIt();
			}
			System.out.println("Done");
		}
	}
	
	private void writeIt() {
		try {
			FileWriter writer = new FileWriter("Auswertung_Bitonic.txt", true);
			if (currentCubeSize == cubeSizeStart) {
				writer.write("Zahlenmenge: "+amountZahlen);
				writer.write(System.lineSeparator());
			}
			writer.write("Kerne: 2^" +bestCube+ "\t");
			writer.write(Integer.toString(min) + " ms");
			writer.write(System.lineSeparator());
			writer.close();
			System.out.println("\nDaten gespeichert.");
		} catch (IOException e) {
			System.out.println("\nSpeichern der Daten gescheitert.");
			e.printStackTrace();
		}
	}
	
	public String checkSorted(int[] arr, int[] cont) {
		if(arr.length != cont.length) {
			return "Zahlen verloren.";
		} else {
			for(int i = 0; i < arr.length; ++i) {
				if(arr[i] != cont[i]) {
					return "Zahlen ungleich: "+arr[i]+" und "+cont[i]+". An position: "+i;
				}
			}
			return "Alle Zahlen sortiert.";
		}
	}
	
	public void generateNumbers(int numbers) {
		Random rand = new Random(seed);
		for (int i = 0; i < numbers; ++i) {
			zahlen[i] = rand.nextInt();
		}
	}
	
	public static void main(String[] args) {
//		args[0] = potenz
//		args[1] = cubeSizeMax
//		args[2] = cubeSizeStart
//		args[3] = cubeSizeInc
//		args[4] = maxRuns
//		args[5] = seed;
		Controller c = new Controller(Integer.parseInt(args[0]), Integer.parseInt(args[1]), 
				Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]),
				Integer.parseInt(args[5]));
		c.doit();
		
	}
}
