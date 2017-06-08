package fagprojekt;

public class TestCC {

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		OnSecondThread(5);
		
		while(System.currentTimeMillis() - time < 25){
		}
		System.out.println(index);
	}

	public static int index = 0;

	public static void OnSecondThread(int i) {
		index = i;
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				index++;
			}
		});
		thread.start();
	}

}
