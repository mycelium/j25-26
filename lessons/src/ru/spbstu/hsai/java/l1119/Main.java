package ru.spbstu.hsai.java.l1119;

import java.util.concurrent.Callable;

public class Main {
	
	
	public static void main(String[] args) {
		System.out.println(Thread.currentThread().getName());
		long start = System.currentTimeMillis();
		long end = 0;
		Thread thread = //new Thread(new Task());
						// new Thread (() -> System.out.print("Hello World"));
						new Thread(new Runnable() {
							@Override
							public void run() {
								System.out.println("Hello World!");
								System.out.println(Thread.currentThread().getName());
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
						});
		thread.start();
		
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		end = System.currentTimeMillis();
		System.out.println(end - start);
		Task taskEntity = new Task(1000, "Hello");
		Thread task = new Thread(taskEntity);
		task.start();
		try {
			task.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		taskEntity.getResult();
		
		Callable<Double> taskWithResult = () -> {return 0.0;};
//				new Callable<Double>() {
//			
//			@Override
//			public Double call() throws Exception {
//				// TODO Auto-generated method stub
//				return null;
//			}
//		};
//		
				
				
				
	
		
		
	}
	
}
