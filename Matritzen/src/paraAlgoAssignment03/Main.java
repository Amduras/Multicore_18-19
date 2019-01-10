package paraAlgoAssignment03;

import java.util.Vector;

public class Main {
  public static void main(String[] args) {



    int[][] matrixA = new int[][] {{1, 2, 3, 4}, {2, 4, 6, 8}, {3, 6, 9, 12}, {4, 8, 12, 16}};
    int[][] matrixB = new int[][] {{1, 0, 0, 0}, {1, 1, 0, 0}, {1, 1, 1, 0}, {1, 1, 1, 1}};
    int[][] resultMatrix = new int[matrixA.length][matrixA.length];

    int processorCount = 16;
    int matrixSize = resultMatrix.length;

    for (int y = 0; y < matrixSize; ++y) {
      for (int x = 0; x < matrixSize; ++x) {
        System.out.print(matrixA[x][y]);
        System.out.print(" ");
      }
      System.out.println();
    }
    System.out.println();
    for (int y = 0; y < matrixSize; ++y) {
      for (int x = 0; x < matrixSize; ++x) {
        System.out.print(matrixB[x][y]);
        System.out.print(" ");
      }
      System.out.println();
    }
    System.out.println();

    Vector<Thread> processorThreads = new Vector<>();

    for (int i = 0; i < processorCount; ++i) {
      processorThreads
          .add(new Thread(new MatrixProcessor(resultMatrix, matrixA, matrixB, i, processorCount)));
      System.out.println();
    }

    for (int i = 0; i < processorCount; ++i) {
      processorThreads.get(i).start();
    }

    for (int i = 0; i < processorCount; ++i) {
      try {
        processorThreads.get(i).join();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    for (int y = 0; y < matrixSize; ++y) {
      for (int x = 0; x < matrixSize; ++x) {
        System.out.print(resultMatrix[x][y]);
        System.out.print(" ");
      }
      System.out.println();
    }
    System.out.println();
  }
}
