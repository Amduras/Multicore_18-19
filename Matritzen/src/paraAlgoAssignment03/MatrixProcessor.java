package paraAlgoAssignment03;

public class MatrixProcessor implements Runnable {
  private int[][] resultMatrix;
  private int[][] matrixA;
  private int[][] matrixB;
  private final int id;
  private int amountOfProcessors;
  private int startXIndex;
  private int startYIndex;
  private int matrixLength;
  private int numbersPerProcessor;

  public MatrixProcessor(int[][] resultMatrix, int[][] matrixA, int[][] matrixB, int id,
      int amountOfProcessors) {
    this.resultMatrix = resultMatrix;
    this.matrixA = matrixA;
    this.matrixB = matrixB;
    this.id = id;
    this.amountOfProcessors = amountOfProcessors;
    this.matrixLength = this.resultMatrix.length;

    int matrixSize = this.matrixLength * this.matrixLength;
    this.numbersPerProcessor = matrixSize / amountOfProcessors;
    double startnumber = id * this.numbersPerProcessor;
    double startposition = startnumber / this.matrixLength;
    System.out.println("Proz: "+this.id+" start: "+startposition);
    double fractionalPart = startposition % 1;
    System.out.println("Proz: "+this.id+" frac: "+fractionalPart);
    double integralPart = startposition - fractionalPart;
    System.out.println("Proz: "+this.id+" int: "+integralPart);


    this.startXIndex = (int) integralPart;
    this.startYIndex = (int) fractionalPart;
    System.out.println("numbers per processor: " + numbersPerProcessor);
    System.out.println("ID: " + id + " Startindex :" + startXIndex + " | " + startYIndex);

    // double fractionalPart = value % 1;
    // double integralPart = value - fractionalPart;
  }

  private void multiply() {
   //Multiply ohne Bubble? Anzeige ist raus!
	int count = 0;
    int x = 0;
    for (int y = this.startYIndex; y < this.matrixLength; ++y) {
      for (x = count == 0 ? this.startXIndex : 0; x < this.matrixLength; ++x) {
        for (int i = 0; i < this.matrixLength; ++i) {
          this.resultMatrix[x][y] += this.matrixA[i][y] * this.matrixB[x][i];
          ++count;
        }
        if (this.id != (amountOfProcessors - 1) && count == this.numbersPerProcessor) {
          break;
        }
      }
      if (this.id != (amountOfProcessors - 1) && count == this.numbersPerProcessor) {
        System.out.println("ID: " + id + " Endindex :" + x + " | " + y);
        break;
      }
    }
  }

  @Override
  public void run() {
    multiply();
  }
}