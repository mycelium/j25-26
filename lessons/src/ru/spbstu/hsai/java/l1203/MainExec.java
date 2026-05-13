package ru.spbstu.hsai.java.l1203;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainExec {
	
	
	public static void main(String[] args) {
		
//		Executor executor = Executors.newFixedThreadPool(4);
//		ExecutorService executorService = Executors.newCachedThreadPool();
//		
//		Future<Double> future =  executorService.submit(new Callable<Double>() {
//			@Override
//			public Double call() throws Exception {
//				Thread.sleep(5000);
////				var temp = 42 / 0;
//				return 42.42;
//			}
//			
//		});
//		
//		try {
//			var result = future.get();
//			System.out.println(result);
//		} catch (InterruptedException | ExecutionException e) {
//			e.printStackTrace();
//		}finally {
//			executorService.close();
//		}
		
		CompletableFuture<Double> cf = CompletableFuture.supplyAsync(()->{
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 42.0;
		});
		
		cf.thenAccept((Double res) -> {
			System.out.println(res);
		});
		
		for (int i = 0; i < 10; i++) {
			System.out.println(i);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		try {
			cf.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
