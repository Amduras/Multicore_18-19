import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class Controller2 {

	// ############# Konfiguration #############
	final static int cubeSizeStart = 10;
	final static int cubeSizeMax = 10;
	final static int cubeSizeInc = 1;
	final static int amountZahlen = 1024;
	final static int maxGeneratorThreads = 8;
	static int amountRuns = 5;
	static int wertebereichMax = Integer.MAX_VALUE;
	// #########################################

	private static int cubeSizeCurrent;
	private static int[] Zahlen;
	private static int[] ZahlenSorted;
	private static int counter = 0;
	private static int digitAmount;
	private static int amountCores = 0;

	private static MyThread2[] threads;
	static CyclicBarrier barrier;

	public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
		Zahlen = new int[amountZahlen];
		ZahlenSorted = new int[amountZahlen];
		int[] ZahlenUnsorted;
		System.out.print("Initialisierung läuft...");
		Zahlen = initializeZahlen(Zahlen);
		System.arraycopy(Zahlen, 0, ZahlenSorted, 0, Zahlen.length);
		System.out.println("\tAbgeschlossen");
		System.out.print("Prüfsortierung läuft....");
		Arrays.parallelSort(ZahlenSorted);
		System.out.println("\tAbgeschlossen");
		final long[] messdaten = new long[amountRuns];
		final List<int[]> datRealMessdaten = new ArrayList<>();
		for (cubeSizeCurrent = cubeSizeStart; cubeSizeCurrent <= cubeSizeMax; cubeSizeCurrent += cubeSizeInc) {
			amountCores = (int) (Math.pow(2, cubeSizeCurrent));
			for (int i = 0; i < amountRuns; ++i) {
				System.out.print("#");
				if ((i + 1) % 5 == 0) {
					System.out.print(" ");
				}
				if ((i + 1) % 10 == 0) {
					System.out.print("\t");
				}
				ZahlenUnsorted = new int[ZahlenSorted.length];
				System.arraycopy(Zahlen, 0, ZahlenUnsorted, 0, Zahlen.length);
				// final long start = System.nanoTime();
				messdaten[i] = doIt(ZahlenUnsorted);
				// System.out.println("Differenz:\t" + ((System.nanoTime() - start) -
				// messdaten[i]) / 1000000.0);
				ZahlenUnsorted = null;
				System.gc();
			}
			final int[] tmp = calculateData(messdaten);
			datRealMessdaten.add(tmp);
			System.out.println();
			System.out.printf(
					"CubeSize:%-5d P:%-5d N:%,d \tMinimum:%-5d Median:%-5d Maximum:%-5d"
							+ System.getProperty("line.separator"),
					cubeSizeCurrent, amountCores, amountZahlen, tmp[0], tmp[1], tmp[2]);
			System.out.println("+------------------------------------------------------------------------+");

		}
		//saveDaten(amountZahlen, datRealMessdaten);

	}

	private static long doIt(int[] numbers) throws InterruptedException, BrokenBarrierException {
		threads = new MyThread2[amountCores];
		barrier = new CyclicBarrier(amountCores + 1);
		System.gc();
		digitAmount = amountZahlen / amountCores;
		for (int i = 0; i < threads.length; ++i) {
			threads[i] = new MyThread2(barrier, i, cubeSizeCurrent);
			final int[] tmp = new int[digitAmount];
			System.arraycopy(numbers, i * digitAmount, tmp, 0, digitAmount);
			threads[i].setNumbers(tmp);
			threads[i].start();
		}

		AbstractMyThread.threadList = threads;

		barrier.await();
		final long start = System.nanoTime();
		barrier.await();

		for (counter = 0; counter < cubeSizeCurrent; ++counter) {
			for (int secondCounter = counter; secondCounter >= 0; --secondCounter) {
				barrier.await();
				barrier.await();
			}
		}
		barrier.await();
		for (counter = 0; counter < cubeSizeCurrent; ++counter) {
			barrier.await();
			barrier.await();
		}
		final long ende = System.nanoTime();

		numbers = updateSorted(numbers);

		if (!checkSorted(numbers)) {
			System.out.println("Sortierung wurde NICHT erfolgreich durchgeführt");
			for (int num : ZahlenSorted) {
				System.out.print(num + "\t");
			}
			System.out.println("");
			for (int num : numbers) {
				System.out.print(num + "\t");
			}
			return 0;
		}
		return ende - start;
	}

	private static int[] updateSorted(int[] numbers) {
		numbers = new int[amountZahlen];
		System.gc();
		for (int i = 0; i < threads.length; ++i) {
			System.arraycopy(threads[i].getNumbers(), 0, numbers, i * digitAmount, digitAmount);
		}
		return numbers;
	}

	private static boolean checkSorted(int[] numbers) {
		boolean bSorted = true;
		int i = 0;
		if (numbers.length == ZahlenSorted.length) {
			while (bSorted && i < ZahlenSorted.length) {
				if (numbers[i] == (ZahlenSorted[i])) {
					++i;
				} else {
					System.out.println(System.getProperty("line.separator")
							+ "Es ist folgende Zahl sortiert nicht vorhanden:\t" + ZahlenSorted[i] + "\tPos:" + i);
					bSorted = false;
				}
			}
		} else {
			bSorted = false;
			System.out.println(
					"Ungleiche Anzahl!\nUnsortiert:\t " + ZahlenSorted.length + "\nSortiert\t " + numbers.length);
		}
		return bSorted;
	}

	private static int[] initializeZahlen(int[] Zahlen) {
		Zahlen = new int[amountZahlen];
		final int amountPerFuture = Zahlen.length / maxGeneratorThreads;
		List<FutureTask<int[]>> taskList = new ArrayList<>();
		ExecutorService service = Executors.newFixedThreadPool(maxGeneratorThreads);
		int rest = Zahlen.length % maxGeneratorThreads;
		for (int i = 0; i < maxGeneratorThreads; ++i) {
			final int currentSeed = i;
			final int maxWerte = wertebereichMax;
			final int currentRest = rest > 0 ? 1 : 0;
			--rest;
			FutureTask<int[]> task = new FutureTask<int[]>(new Callable<int[]>() {
				private int seed = currentSeed;
				private int amount = amountPerFuture + currentRest;

				@Override
				public int[] call() {
					final int[] zahlen = new int[amount];
					Random rand = new Random(seed);
					for (int i = 0; i < zahlen.length; ++i) {
						zahlen[i] = rand.nextInt(maxWerte);
					}
					return zahlen;

				}
			});
			taskList.add(task);
			service.execute(task);
		}
		int currentRest = 0;
		for (int i = 0; i < taskList.size(); ++i) {
			int[] tmp = new int[0];
			try {
				tmp = taskList.get(i).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			if (tmp != null && tmp.length > 0) {
				System.arraycopy(tmp, 0, Zahlen, (i * amountPerFuture) + currentRest, tmp.length);
				if (tmp.length % amountPerFuture == 1) {
					++currentRest;
				}
			} else {
				System.out.println("Fehler in der Zahlengenerierung!");
			}
		}
		service.shutdown();
		// if (Zahlen.length % amountCores != 0) {
		// int log = (int) (Math.log(Zahlen.length) / Math.log(2));
		// ++log;
		// final int[] ZahlenNew = new int[(int) Math.pow(log, 2)];
		// final double mitte = ZahlenNew.length - Zahlen.length / 2.0;
		// final int diffLinks; // diffRechts wird nicht benötigt, siehe zweite
		// For-Schleife
		// if (mitte % 1 == 0.5) {
		// diffLinks = (int) Math.round(mitte - 1);
		// } else {
		// diffLinks = (int) mitte;
		// }
		// for (int i = 0; i < diffLinks; ++i) {
		// ZahlenNew[i] = 0;
		// }
		// System.arraycopy(Zahlen, 0, ZahlenNew, diffLinks, Zahlen.length);
		// for (int i = diffLinks + Zahlen.length; i < ZahlenNew.length; ++i) {
		// ZahlenNew[i] = 0;
		// }
		// return ZahlenNew;
		// }
		return Zahlen;

	}

	private static int[] calculateData(long[] array) {
		Arrays.sort(array);
		final int[] array2 = new int[3];
		array2[0] = (int) (array[0] / 1000000.0);

		if (array.length % 2 == 0) {
			long tmp = array[array.length / 2];
			tmp += array[array.length / 2 - 1];
			array2[1] = (int) ((tmp / 2.0) / 1000000.0);
		} else {
			array2[1] = (int) (array[array.length / 2] / 1000000.0);
		}

		array2[2] = (int) (array[array.length - 1] / 1000000.0);
		return array2;
	}

	private static void saveDaten(int zahlen, List<int[]> datRealMessdaten) {
		String timestamp = LocalDateTime.now().toString().substring(0, LocalDateTime.now().toString().length() - 4);
		timestamp = timestamp.replace(':', '-');
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(timestamp + "_" + zahlen + ".txt"), "utf-8"))) {
			writer.write("CubeSize\tMin\tMed\tMax" + System.getProperty("line.separator"));
			int k = cubeSizeStart;
			for (int[] array : datRealMessdaten) {
				writer.write(k + "\t" + Math.round(array[0]) + "\t" + Math.round(array[1]) + "\t" + Math.round(array[2])
						+ "\t");
				writer.write("" + System.getProperty("line.separator"));
				k += cubeSizeInc;
			}
			writer.close();
		} catch (IOException e) {
			System.out.println("Speichern der Messdaten fehlgeschlagen!");
			e.printStackTrace();
		}
	}

}
