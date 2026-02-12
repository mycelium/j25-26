package ru.spbstu.hsai.java.exam;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Frolov{

	private Object monitor = new Object();
	
	
	public synchronized void doSmth() {
		
	}
	
	public void doSmth2() {
		synchronized(this)
		{
			System.out.println("");
		}
	}
	
	public static synchronized void doSmthStatic() {
		
	}
	
	public static void doSmthStatic2() {
		synchronized(Frolov.class)
		{
			System.out.println("");
		}
	}
	
	public void veryImportantLogic() {
		
		ReadWriteLock rwLock = new ReentrantReadWriteLock();
		
		
	}
}
