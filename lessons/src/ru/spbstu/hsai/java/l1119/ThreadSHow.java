package ru.spbstu.hsai.java.l1119;

public class ThreadSHow {
	public static void main(String[] args) {
		
		new Thread(()->{
			Thread.currentThread().setName("Hello");
			while(true) {
				System.out.println("tick");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
}
