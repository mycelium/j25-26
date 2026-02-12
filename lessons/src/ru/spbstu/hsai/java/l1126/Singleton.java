package ru.spbstu.hsai.java.l1126;


public class Singleton {
	
	private Singleton() {
	}
	
	private static Singleton instance;
	private static Object monitor = new Object();
	
	
	public static Singleton getInstance() {
		if (instance == null) {
			synchronized (monitor) {
				if (instance == null) {
					instance = new Singleton();
				}
			}
		}
		return instance;
	}
	
}
