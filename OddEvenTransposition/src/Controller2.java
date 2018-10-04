import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
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
	private final static Integer MERGESORT = 0, ARRAYSORT = 1, BUBBLESORT = 2;

	// *************** Konfiguration *****************
	static boolean printIt = false;
	static boolean logDigits = true;
	static boolean logSteps = false;
	static boolean logTime = false;
	static int startSorter = ARRAYSORT;
	static int currentSorter = MERGESORT;
	static int threadNum = 1;
	static int maxThreads = 16;
	static int maxGeneratorThreads = 4;
	// Amount für SunMAchine 200.000.000-300.000.000 zahlen
	static int amountZahlen = 300000;
	static int amountRuns = 10;
	static int wertebereichMax = Integer.MAX_VALUE;
	// ***********************************************

	private static int[] sorted;
	private static int[] Zahlen;
	private static int[] ZahlenSorted;
	private static int counter = 0;
	private static int digitAmount;

	private static MyThread2[] threads;
	static CyclicBarrier barrier;

	public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
		final int[][][] messdaten = new int[2][maxThreads / 2][3];
		final int startThreadnum = threadNum;
		for (int starter = ARRAYSORT; starter <= BUBBLESORT; ++starter) {
			if (starter == BUBBLESORT) {
				amountZahlen /= 100;
				amountRuns /= amountRuns == 1 ? 1 : 2;
			}

			startSorter = starter;
			for (threadNum = startThreadnum; threadNum <= maxThreads; ++threadNum) {
				if (threadNum > 2) {
					threadNum += 1;
				}
				Zahlen = new int[amountZahlen];
				ZahlenSorted = new int[amountZahlen];

				final long generateStart = System.nanoTime();
				initializeZahlen();
				final long generateEnde = System.nanoTime();
				if (logTime) {
					System.out.println(
							"Benötigte Zeit zum Generieren in ms:\t" + ((generateEnde - generateStart) / 1000000.0));
				}
				System.arraycopy(Zahlen, 0, ZahlenSorted, 0, Zahlen.length);
				Arrays.parallelSort(ZahlenSorted);
				final long[] saverArray = new long[amountRuns];
				System.out.print("N von " + amountRuns + " Schritte:\t");
				for (int i = 0; i < amountRuns; ++i) {
					System.out.print("#");
					if ((i + 1) % 5 == 0) {
						System.out.print(" ");
					}
					if ((i + 1) % 10 == 0) {
						System.out.print("\t");
					}
					saverArray[i] = doIt();
				}
				System.out.println();
				messdaten[startSorter - 1][(int) Math.round((threadNum / 2.0)) - 1] = calculateData(saverArray);
				System.out.println("Start-Sortierung:\t" + (startSorter == ARRAYSORT ? "Arrays.sort()" : "Bubblesort"));
				System.out.println("Laufzeit-Sortierung:\tMergesort");
				System.out.printf(
						"P:%-5d N:%-10d Minimum:%-5d Median:%-5d Maximum:%-5d" + System.getProperty("line.separator"),
						threadNum, amountZahlen, messdaten[startSorter - 1][(int) Math.round((threadNum / 2.0)) - 1][0],
						messdaten[startSorter - 1][(int) Math.round((threadNum / 2.0)) - 1][1],
						messdaten[startSorter - 1][(int) Math.round((threadNum / 2.0)) - 1][2]);

				System.out.println("+--------------------------------------------------------+");
			}
			if (starter == BUBBLESORT) {
				amountZahlen *= 100;
			}
		}
		if (logDigits) {
			saveDaten(currentSorter, amountZahlen, messdaten);
		}

	}

	public static long doIt() throws InterruptedException, BrokenBarrierException {
		final long sortStart = System.nanoTime();
		counter = 0;
		digitAmount = Zahlen.length / threadNum;
		threads = new MyThread2[threadNum];
		barrier = new CyclicBarrier(threadNum + 1);
		final long gcStart = System.nanoTime();
		System.gc();
		final long gcEnde = System.nanoTime();
		if (logTime) {
			System.out.println("\n Benötigte Zeit zum GC in ms:\t" + ((gcEnde - gcStart) / 1000000.0));
		}

		int rest = Zahlen.length % threadNum;
		int currentRest = 0;
		for (int i = 0; i < threads.length; ++i) {
			final int[] digits;
			if (rest > 0) {
				digits = new int[digitAmount + 1];
				--rest;
			} else {
				digits = new int[digitAmount];
			}
			System.arraycopy(Zahlen, (i * digitAmount) + currentRest, digits, 0, digits.length);
			if (digits.length % digitAmount == 1) {
				++currentRest;
			}
			threads[i] = new MyThread2(digits, threadNum, i, barrier, startSorter);
		}
		counter = -2;
		printCores();
		for (int i = 0; i < threads.length - 1; ++i) {
			threads[i].setNext(threads[i + 1]);
		}
		for (int i = 1; i < threads.length; ++i) {
			threads[i].setPrev(threads[i - 1]);
		}
		final Long start = System.nanoTime();
		for (int i = 0; i < threads.length; ++i) {
			threads[i].start();
		}

		barrier.await();
		if (logTime && logSteps) {
			System.out.println("Vorsortierung:\t" + ((System.nanoTime() - start) / 1000000.0));
		}
		counter = -1;
		printCores();
		counter = 0;
		long beforeSort = System.nanoTime();
		while (counter < threadNum) {
			barrier.await();
			if (logTime && logSteps) {
				System.out.print(
						counter + 1 + ". Sortierung:\t" + ((System.nanoTime() - beforeSort) / 1000000.0) + "\t\t");
				beforeSort = System.nanoTime();
			}
			barrier.await();

			printCores();
			if (logTime && logSteps) {
				System.out.println(counter + 1 + ". Zuordnung:\t" + ((System.nanoTime() - beforeSort) / 1000000.0));
				beforeSort = System.nanoTime();
			}
			++counter;

		}
		final long ende = System.nanoTime();
		if (!checkSorted()) {
			System.out.println("Sortierung wurde NICHT erfolgreich durchgeführt");
		}
		if (printIt) {
			System.out.println("Benötigte Zeit in ns:\t" + (ende - start));
			// S = NS / 1000000000
			// MS = NS / 1000000
			System.out.println("Zahlen:\t\t" + new DecimalFormat().format(Zahlen.length));
			System.out.println("CPUs:\t\t" + threadNum);
			System.out.println("Start-Sortierung:\t" + (startSorter == ARRAYSORT ? "Arrays.sort()"
					: startSorter == BUBBLESORT ? "Bubblesort" : "Mergesort"));
			System.out.println("Laufzeit-Sortierung:\t" + (currentSorter == ARRAYSORT ? "Arrays.sort()"
					: currentSorter == BUBBLESORT ? "Bubblesort" : "Mergesort"));
		} else if (logTime) {
			System.out.println("Benötigte Zeit in ms:\t" + ((ende - start) / 1000000.0));

			long sortTime = System.nanoTime() - sortStart;
			sortTime -= (ende - start);
			sortTime -= (gcEnde - gcStart);
			System.out.println("Nicht registiert:\t" + sortTime / 1000000.0);

		}
		return ende - start;

	}

	private static void printCores() {
		if (printIt) {
			int rest = updateSorted();
			String dividor = "";
			for (int i = 0; i < digitAmount + rest % 1; ++i) {
				dividor += "---------";
			}
			final String zusatz = (counter < 0 ? counter == -2 ? " Initialisiert :" : " Vorsortiert :"
					: counter % 2 == 0 ? "gerade :" : "ungerade :");
			System.out.println("---------------" + dividor + "\n" + counter + ". Schritt - " + zusatz);
			for (int i = 0; i < threads.length; ++i) {
				System.out.print("Kern Nr.: " + i + "\t");
				for (int j = 0; j < threads[i].getDigits().length; ++j) {
					System.out.print(threads[i].getDigits()[j] + "\t");
				}
				System.out.println("");
			}
		}
	}

	private static int updateSorted() {
		sorted = new int[Zahlen.length];
		int rest = 0;
		for (int i = 0; i < threads.length; ++i) {
			System.arraycopy(threads[i].getDigits(), 0, sorted, (i * digitAmount) + rest,
					threads[i].getDigits().length);
			if (threads[i].getDigits().length % digitAmount == 1) {
				++rest;
			}
		}
		return rest;
	}

	private static boolean checkSorted() {
		updateSorted();
		boolean bSorted = true;
		int i = 0;
		if (sorted.length == ZahlenSorted.length) {
			Arrays.sort(ZahlenSorted);
			while (bSorted && i < ZahlenSorted.length) {
				if (sorted[i] == (ZahlenSorted[i])) {
					++i;
				} else {
					System.out.println(
							"Es ist folgende Zahl sortiert nicht vorhanden:\t" + ZahlenSorted[i] + "\tPos:" + i);
					bSorted = false;
				}
			}
		} else {
			bSorted = false;
			System.out.println(
					"Ungleiche Anzahl!\nUnsortiert:\t " + ZahlenSorted.length + "\nSortiert\t " + sorted.length);
		}
		return bSorted;
	}

	private static void initializeZahlen() {
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
	}

	private static void saveDaten(int sorter, int zahlen, int[][][] messdaten) {
		String timestamp = LocalDateTime.now().toString().substring(0, LocalDateTime.now().toString().length() - 4);
		timestamp = timestamp.replace(':', '-');
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(timestamp + "_" + zahlen + ".txt"), "utf-8"))) {
			writer.write("Kerne\tAS-Min\tAS-Med\tAs-Max\t");
			writer.write("BS-Min\tBS-Med\tBs-Max" + System.getProperty("line.separator"));
			int k = 1;
			for (int i = 1; i <= messdaten[0].length; ++i) {
				// writer.write(i + "\t" + Math.round(messdaten[0][i - 1]) + "\t" +
				// Math.round(messdaten[1][i - 1]) + "\t"
				// + System.getProperty("line.separator"));

				k += i > 2 ? 1 : 0;
				writer.write(k + "\t" + Math.round(messdaten[0][i - 1][0]) + "\t" + Math.round(messdaten[0][i - 1][1])
						+ "\t" + Math.round(messdaten[0][i - 1][2]) + "\t");
				writer.write(Math.round(messdaten[1][i - 1][0]) + "\t" + Math.round(messdaten[1][i - 1][1]) + "\t"
						+ Math.round(messdaten[1][i - 1][2]) + "\t");
				writer.write("" + System.getProperty("line.separator"));
				++k;
			}
			writer.close();
		} catch (IOException e) {
			System.out.println("Speichern der Messdaten fehlgeschlagen!");
			e.printStackTrace();
		}
	}

	private static int[] calculateData(long[] array) {
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

}
