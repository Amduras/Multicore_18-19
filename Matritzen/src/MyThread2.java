import java.awt.Point;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class MyThread2 extends Thread {

	CyclicBarrier barrier;
	int id;

	Point pStart, pDiff, pEnde;

	static double[][] result;
	static double[][] a;
	static double[][] b;
	private double tmpResult;

	public MyThread2(int id, CyclicBarrier barrier, Point start, Point dimsize) {
		this.id = id;
		this.barrier = barrier;
		this.pStart = start;
		this.pDiff = dimsize;
		pEnde = new Point((int) (pStart.getX() + pDiff.getX()), (int) (pStart.getY() + pDiff.getY()));
	}

	@Override
	public void run() {
		final int pStartY = (int) pStart.getY();
		final int pStartX = (int) pStart.getX();
		final int pEndeY = (int) pEnde.getY();
		final int pEndeX = (int) pEnde.getX();

		myWait();
		myWait();
		for (int j = (int) pStartX; j < pEndeX; ++j) {
			for (int k = (int) pStartY; k < pEndeY; ++k) {
				tmpResult = 0;
				for (int i = 0; i < a[0].length; ++i) {
					tmpResult += a[j][i] * b[k][i];
				}
				result[j][k] = tmpResult;
			}
		}
		myWait();
	}

	private void myWait() {
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
	}
}
