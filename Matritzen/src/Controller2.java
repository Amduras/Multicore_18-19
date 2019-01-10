import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Controller2 {
	// static double[][] a = { { 1, 2, 3, 4 }, { 2, 4, 6, 8 }, { 3, 6, 9, 12 }, { 4,
	// 8, 12, 16 } };
	// static double[][] b = { { 1, 0, 0, 0 }, { 1, 1, 0, 0 }, { 1, 1, 1, 0 }, { 1,
	// 1, 1, 1 } };

	private static int matrixSize = 16;

	static double[][] a = new double[matrixSize][matrixSize];
	static double[][] b = new double[matrixSize][matrixSize];

	static double[][] tmpResult = new double[matrixSize][matrixSize];

	private static int CPUlength = 8, CPUwidth = 8;
	private static int amountCores = CPUlength * CPUwidth;

	private static Point dimSize;
	static List<MyThread2> threadList = new ArrayList<>();

	private static double seqTime;

	public static void main(String[] args) throws Exception {
		if ((matrixSize % CPUlength != 0) || (matrixSize % CPUwidth != 0)) {
			throw new Exception("Ungütlige Anzahl an verfügbaren CPUs!" + System.getProperty("line.separator")
					+ "Länge:" + (matrixSize % CPUlength) + "\tBreite:" + (matrixSize % CPUwidth));
		}
		System.out.print("Initialisierung läuft...");
		initMatrix();
		System.out.println("Fertig");

		MyThread2.a = a;
		MyThread2.b = b;
		MyThread2.result = new double[matrixSize][matrixSize];

		dimSize = new Point(a.length / CPUwidth, b.length / CPUlength);
		for (int i = 0; i < 10; ++i) {
			final double paraTime = doIt() / 1000000.0;

			System.out.printf(
					"P:%-2d\tQ:%-2d\tP*Q:%4d\tM:%-5d \t|Seq:%-5.2f\tParallel:%-5.2f\tSpeedup:%-2.4f"
							+ System.getProperty("line.separator"),
					CPUlength, CPUwidth, CPUlength * CPUwidth, matrixSize, seqTime, paraTime, (seqTime / paraTime));
			System.out.println(
					"+-------------------------------------------------------------------------------------------------------+");
		}
	}

	private static long doIt() {
		CyclicBarrier barrier = new CyclicBarrier(amountCores + 1);
		int id = 0;
		for (int i = 0; i < CPUwidth; ++i) {
			for (int j = 0; j < CPUlength; ++j) {

				final Point start = new Point((int) (i * dimSize.getX()), (int) (j * dimSize.getY()));
				final MyThread2 thread = new MyThread2(id++, barrier, start, dimSize);
				thread.start();
				threadList.add(thread);
			}
		}
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e1) {
			e1.printStackTrace();
		}
		final long start = System.nanoTime();
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e1) {
			e1.printStackTrace();
		}

		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
		if (checkIt()) {
			return System.nanoTime() - start;
		} else {
			return 0;
		}

	}

	private static void initMatrix() {
		final Random randA = new Random(1);
		final Random randB = new Random(2);

		for (int i = 0; i < matrixSize; ++i) {
			for (int j = 0; j < matrixSize; ++j) {
				a[i][j] = randA.nextDouble();
				b[i][j] = randB.nextDouble();
			}
		}
		System.out.print("Arrays erzeugt...");
		final long tStart = System.nanoTime();
		for (int i = 0; i < matrixSize; ++i) {
			for (int j = 0; j < matrixSize; ++j) {
				tmpResult[i][j] = 0;
				for (int k = 0; k < a[i].length; ++k) {
					tmpResult[i][j] += a[i][k] * b[j][k];
				}
			}
		}
		seqTime = ((System.nanoTime() - tStart) / 1000000.0);
	}

	private static boolean checkIt() {
		boolean failed = true;
		for (int i = 0; i < matrixSize && failed; ++i) {
			for (int j = 0; j < matrixSize && failed; ++j) {
				if (tmpResult[i][j] != MyThread2.result[i][j]) {
					failed = false;
					System.out.println(
							"Fehler in Zelle " + i + "\t" + j + "\t" + MyThread2.result[i][j] + "\t" + tmpResult[i][j]);
				}
			}
		}

		if (!failed) {
			System.out.println("Falsch berechnet!");
			printMatrix();
			return false;
		} else {
			System.out.println("Sortierung erfolgreich!");
			return true;
		}
	}

	static void printMatrix() {
		for (int i = 0; i < matrixSize; ++i) {
			for (int j = 0; j < matrixSize; ++j) {
				System.out.print(a[i][j] + "\t");
			}
			System.out.print("\t|\t");
			for (int j = 0; j < matrixSize; ++j) {
				System.out.print(b[i][j] + "\t");
			}
			System.out.println("");
		}
		System.out.println("+-\t-\t-\t-\t-\t-\t-\t-\t-\t-\t-\t-\t-\t+\t-\t-\t-\t-\t-\t-\t-\t-\t-\t-\t-\t-\t-+");

		for (int i = 0; i < matrixSize; ++i) {
			for (int j = 0; j < matrixSize; ++j) {
				System.out.print(MyThread2.result[i][j] + "\t");
			}
			System.out.print("\t|\t");
			for (int j = 0; j < matrixSize; ++j) {
				System.out.print(tmpResult[i][j] + "\t");
			}
			System.out.println("");
		}
	}

	private static void myWait(CyclicBarrier barrier) {
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
