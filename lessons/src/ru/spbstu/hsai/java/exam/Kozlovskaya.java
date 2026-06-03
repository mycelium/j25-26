package ru.spbstu.hsai.java.exam;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Kozlovskaya {
	
	public void dosomething() {}
	
	
	private static class Task implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static void main(String[] args){
		
		
		
		Thread mythread1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
		});
		Thread mythread2 = new Thread(()->{
			
		});
		Thread mythread3 = new Thread();
		Thread mythread4 = new Thread();
		Thread mythread5 = new Thread();
			
		
		ReentrantLock mylock1 = new ReentrantLock();
		ReentrantLock mylock2 = new ReentrantLock();
		ReentrantLock mylock3 = new ReentrantLock();
		ReentrantLock mylock4 = new ReentrantLock();
		ReentrantLock mylock5 = new ReentrantLock();
		
		mythread1.start();
		
		
		mythread2.start();
		mythread3.start();
		mythread4.start();
		mythread5.start();
		
		try {
			mylock1.lock();
		}finally {
			mylock1.unlock();
		}
	}
	
	
	
	
	
	
	
	
}
