import java.awt.Point;
import java.util.concurrent.CountDownLatch;

public class MyThread extends Thread{
	
	private int id;
	private double[][] matA;
	private double[][] matB;
	private double[][] resultMatrix;
	private CountDownLatch donesignal;
	Point pStart, pEnde;
	public MyThread(int id, double[][] matA, double[][] matB, double[][] 
			resultMatrix ,CountDownLatch donesignal, Point start, Point dimSize) {
		this.id = id;
		this.matA = matA;
		this.matB = matB;
		this.resultMatrix = resultMatrix;
		this.donesignal = donesignal;
		this.pStart = start;
		this.pEnde = new Point((int) (pStart.getX() + dimSize.getX()), (int) (pStart.getY() + dimSize.getY()));
	}
	
	@Override
	public void run() {
		final int pStartY = (int) pStart.getY();
		final int pStartX = (int) pStart.getX();
		final int pEndeY = (int) pEnde.getY();
		final int pEndeX = (int) pEnde.getX();
		
		System.out.println();
		System.out.println("Thread: "+id+"\nStartX: "+pStartX+" StartY: "+pStartY+"\nEndeX: "+pEndeX+" EndeY: "+pEndeY);
		
		for (int i = pStartX; i < pEndeX; ++i) {
			for (int k = 0; k < matA.length; ++k) {
			for (int j = pStartY; j < pEndeY; ++j) {
					resultMatrix[i][j] += matA[i][k] * matB[k][j];
				}
			}
		}
		
		donesignal.countDown();
	}
}
