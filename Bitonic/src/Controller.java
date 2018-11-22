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
	final int currentCubeSize;
	final int seed;
	final int maxRuns;
//	#########################
	
	int cores;
	int[] zahlen;
	int[] testFolge = {3, 7, 4, 8, 6, 2, 1, 5};
	int[] sorted;
	int[] controll;
	MyThread[] threads;
	CyclicBarrier barrier;
	CountDownLatch doneSignal;
	int coreNumbers;
	
	Controller(int amountZahlen, int cuubeSizeMax, int cubeSizeStart, int cubeSizeInc, int maxRuns, int seed){
		this.amountZahlen = amountZahlen;
		this.cubeSizeMax = cuubeSizeMax;
		this.cubeSizeStart = cubeSizeStart;
		this.cubeSizeInc = cubeSizeInc;
		this.maxRuns = maxRuns;
		this.seed = seed;
		zahlen = new int[amountZahlen];
		sorted = new int[amountZahlen];
		currentCubeSize = 3;
		threads = new MyThread[(int) Math.pow(2, currentCubeSize)];
		barrier = new CyclicBarrier(threads.length);
		doneSignal = new CountDownLatch(threads.length);
	}
	
	public void doit() {
		System.out.println("Initialisierung");
		generateNumbers(amountZahlen);
		for(int i = 0; i < zahlen.length; ++i) {
			System.out.print(zahlen[i]+" ");
		}
		System.out.println("\nAbgeschlossen");
		System.arraycopy(zahlen, 0, sorted, 0, zahlen.length);
		Arrays.parallelSort(sorted);
		
		coreNumbers = amountZahlen / threads.length;
		for(int i = 0; i < threads.length; ++i) {
			threads[i] = new MyThread(i, barrier, doneSignal, threads, currentCubeSize);
			final int[] tmp = new int[coreNumbers];
			System.arraycopy(zahlen, i * coreNumbers, tmp, 0, coreNumbers);
			threads[i].setNumbers(tmp);
		}
		
		for(int i = 0; i < threads.length; ++i) {
			threads[i].start();
		}
		
		try {
			doneSignal.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Done");
		
		for (int i = 0; i < threads.length; ++i) {
			System.arraycopy(threads[i].getNumbers(), 0, sorted, (i * coreNumbers),
					threads[i].getNumbers().length);
		}
		
		for(int i = 0; i < sorted.length; ++i) {
			System.out.print(sorted[i]);
			if((i+1) % coreNumbers == 0) {
				System.out.print(" ");
			} else {
				System.out.print("-");
			}
		}
		/*cores = (int) (Math.pow(2, currentSize));
		for (int i = 0; i < maxRuns; ++i) {
			System.out.print("#");
			if ((i + 1) % 5 == 0) {
				System.out.print(" ");
			}
			if ((i + 1) % 10 == 0) {
				System.out.print("\t");
			}
			
			System.gc();
		}*/
	}
	
	public void generateNumbers(int numbers) {
		Random rand = new Random(seed);
		for (int i = 0; i < numbers; ++i) {
			zahlen[i] = rand.nextInt(100);
			//zahlen[i] = testFolge[i];
		}
	}
	
	public static void main(String[] args) {
//		args[0] = amountzahlen
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
