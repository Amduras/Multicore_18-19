import java.awt.Point;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Matritze {
	
	double[][] matA = {{1,2,3,4}, {2,4,6,8}, {3,6,9,12}, {4,8,12,16}};
	double[][] matB = {{1,1,1,1}, {0,1,1,1}, {0,0,1,1}, {0,0,0,1}};
	int matrixSize = 4;
	int coresWidth = 2;
	int coresLength = 2;
//	double[][] matA = new double[matrixSize][matrixSize];
//	double[][] matB = new double[matrixSize][matrixSize];
	double[][] resultMatrix = new double[matrixSize][matrixSize];
	int cores = coresWidth*coresLength;
	MyThread[] threads = new MyThread[cores];
	CountDownLatch doneSignal = new CountDownLatch(threads.length);
	double coreNumbers = Math.pow(resultMatrix.length, 2) / threads.length;
	Point dimSize = new Point(matA.length / threads.length, matB.length / threads.length);
	DecimalFormat df = new DecimalFormat("0.00");
	
	public Matritze() {
//		init();
//		System.out.println("Matrix A");
//		printIt(matA);
//		System.out.println("Matrix B");
//		printIt(matB);
	}
	
	public void init() {
		Random randA = new Random(2);
		Random randB = new Random(4);
		for(int i = 0; i < matA.length; ++i) {
			for(int j = 0; j < matA[i].length; ++j) {
				matA[i][j] = randA.nextDouble()*10;
			}
		}
		
		for(int i = 0; i < matB.length; ++i) {
			for(int j = 0; j < matB[i].length; ++j) {
				matB[i][j] = randB.nextDouble()*10;
			}
		}
		
//		transpose();
	}
	//points anpassen, zeitmessung,kerne hochlaufen
	public void doIt() {
		System.out.println(coreNumbers);
		if ((matrixSize * matrixSize) % cores == 0) {
			int id = 0;
			for (int i = 0; i < coresWidth; ++i) {
				for (int j = 0; j < coresLength; ++j) {
					final Point start = new Point((int) (i * dimSize.getX()), (int) (j * dimSize.getY()));
					System.out.println("Thread: "+id+" gestartet");
					threads[id] = new MyThread(id++, matA, matB, resultMatrix, doneSignal, start, dimSize);
				}
			}
			long start = System.nanoTime();
			for (int i = 0; i < threads.length; ++i) {
				threads[i].start();
			}

			try {
				doneSignal.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			long end = System.nanoTime();
			System.out.println("Zeit: \n" + ((end - start) / 1000000000));
			 System.out.println("Result Matrix");
			 printIt(resultMatrix);
		} else {
			System.out.println("Zahlen lassen sich nicht aufteilen");
		}
	}
	
	public void printIt(double[][] Matrix) {
		for(int i = 0; i < Matrix.length; ++i) {
			for(int j = 0; j < Matrix[i].length; ++j) {
				System.out.print(df.format(Matrix[i][j]) + "\t");
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		Matritze m = new Matritze();
		m.doIt();
	}
}
