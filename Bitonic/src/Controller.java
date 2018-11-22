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
	final int seed;
	final int maxRuns;
//	#########################
	
	int currentSize = 0;
	int cores;
	int[] zahlen;
//	int[] testFolge = {2, 5, 6, 10, 8, 7, 4, 1};
	int[] testFolge = {3, 7, 4, 8, 6, 2, 1, 5};
	int[] sorted;
	int[] controll;
	MyThread[] threads;
	CyclicBarrier barrier;
	CountDownLatch doneSignal;
	
	Controller(int amountZahlen, int cuubeSizeMax, int cubeSizeStart, int cubeSizeInc, int maxRuns, int seed){
		this.amountZahlen = amountZahlen;
		this.cubeSizeMax = cuubeSizeMax;
		this.cubeSizeStart = cubeSizeStart;
		this.cubeSizeInc = cubeSizeInc;
		this.maxRuns = maxRuns;
		this.seed = seed;
		zahlen = new int[amountZahlen];
		sorted = new int[amountZahlen];
		threads = new MyThread[amountZahlen];
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
		
		for(int i = 0; i < zahlen.length; ++i) {
			threads[i] = new MyThread(i, barrier, doneSignal, threads);
			final int[] tmp = new int[1];
			System.arraycopy(zahlen, i, tmp, 0, 1);
			threads[i].setNumbers(tmp);
		}
		
		for(int i = 0; i < threads.length; ++i) {
			System.out.print(threads[i].numbers[0]+" ");
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
			//zahlen[i] = rand.nextInt(100);
			zahlen[i] = testFolge[i];
		}
	}
	
	public static void main(String[] args) {
//		args[0] = amountzahlen
//		args[1] = cubeSizeMax
//		args[2] = cubeSizeStart
//		args[3] = cubeSizeInc
//		args[4] = maxRuns
//		args[5] = seed;
		
//		args[0] = "8";
//		args[1] = "10";
//		args[2] = "1";
//		args[3] = "1";
//		args[4] = "1";
//		args[5] = "42";
		Controller c = new Controller(Integer.parseInt(args[0]), Integer.parseInt(args[1]), 
				Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]),
				Integer.parseInt(args[5]));
		c.doit();
	}
}
