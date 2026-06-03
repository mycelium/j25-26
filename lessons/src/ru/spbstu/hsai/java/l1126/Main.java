package ru.spbstu.hsai.java.l1126;

public class Main {
	
	
	public static int resource = 0;
	
	public static void main(String[] args) {
		
		Object monitor = new Object();
		
		Thread thread1 = new Thread(() -> {
			
			synchronized (monitor) {				
				Main.resource++;
			}
			
		});
		
		Thread thread2 = new Thread(() -> {
			
			synchronized (monitor) {
				Main.resource++;
			}
			
		});
		
		thread1.start();
		thread2.start();
		
		try {
			thread1.join();
			thread2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(resource);
		
	}
	
	
}
