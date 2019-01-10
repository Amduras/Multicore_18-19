
public class test {
	public static void main(String[] args) {
		int zahlen = (int) Math.pow(2, 30);
		for(int i = 0; i <= 10; ++i) {
			if(zahlen % Math.pow(2, i) == 0) {
				System.out.println(i);
			}
		}
	}
}
