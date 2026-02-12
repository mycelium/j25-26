package ru.spbstu.hsai.java.l1210;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Synchromizers {
	public static void main(String[] args) {
		Lock lock = new ReentrantLock();
		Condition condition =  lock.newCondition();
		
		lock.lock();
		
		
		try {
			condition.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		condition.signal();
		
		lock.unlock();
		
		
		Map<String, Integer> data = new ConcurrentHashMap<String, Integer>();
		
	
	}
}
