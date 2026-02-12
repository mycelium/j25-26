package ru.spbstu.hsai.java.l1203;

public class Main {

	public volatile static double shared = 1.0;

	public static void main(String[] args) {

		Thread thread1 = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				Main.shared +=1.0;
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		Thread thread2 = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				Main.shared /=2;
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		Thread thread3 = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				Main.shared *=2;
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		
		thread1.start();
		thread2.start();
		thread3.start();
		long start = System.currentTimeMillis();
		
		while(System.currentTimeMillis() - start < 20000) {
			System.out.println(shared);
			try {
				Thread.currentThread().sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		thread1.interrupt();
		thread2.interrupt();
		thread3.interrupt();
		
		System.out.println(thread1.getState().name());
		System.out.println(thread2.getState().name());
		System.out.println(thread3.getState().name());
		
		System.out.println(shared);
		
	}

}
