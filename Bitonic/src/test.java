
public class test {
//	public static void main(String[] args) {
//		int counter = 5;
//		int secondCounter = 5;
//		int myId = 2;
//		
////		while(secondCounter >= 0) {
////			//System.out.println(myId ^ 1 << secondCounter);
////			System.out.println(myId % (int) (Math.pow(2, counter + 2)));
////			--secondCounter;
////		}
//		System.out.println(" "+Integer.toBinaryString(myId));
//		System.out.println(Integer.toBinaryString(secondCounter));
//		System.out.println(Integer.toBinaryString(myId ^ secondCounter));
//		
//		int signal[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,1,0,0,1,0,1,0,1,0,1,1,0,1,0,1,0,0,0,0,1,1,0,0,0,0,1,1,0,1,0,0,0,0};
//		System.out.println(signal.length);
//	}
	boolean UP = true;
	boolean DOWN = false;
	int[] numbers = {3, 7, 4, 8, 6, 2, 1, 5};
	public void sort(int[] a, int start, int end, boolean up) {
		if(end > 1) {
			int k = end/2;
			System.out.println(end+" "+up);
			sort(a, start, k, UP);
			sort(a, start+k, k, DOWN);
		}
		
	}
	
	public void test() {
		int stages = 0;
		int size = 3;
		int steps = 0;
		
		while(stages < size) {
			steps = stages;
			while(stages >= 0) {
				//int othernumber = numbers[] ^ 1 << stages;
			}
		}
	}
	
	public void printArray(int[] a) {
		for (int i=0; i<a.length; ++i) 
            System.out.print(a[i] + " "); 
        System.out.println(); 
	}
	public static void main(String[] args) {
		
		test t = new test();
		t.sort(t.numbers, 0, t.numbers.length, t.UP);
		t.test();
		//t.printArray(t.folge);
	}
}
