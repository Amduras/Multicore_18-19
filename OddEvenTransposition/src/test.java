
public class test {
	
	public static void main(String[] args) {
		int a,b;
		a = 4;
		b = 5;
		
		a = a | b;
		b = a | b;
		a = a | b;
		System.out.println(a+" "+b);
	}
}
