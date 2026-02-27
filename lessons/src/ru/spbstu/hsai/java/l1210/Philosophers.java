package ru.spbstu.hsai.java.l1210;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Philosophers {

	public static void main(String[] args) {
//		Random rand = new Random();
//
//		var count = rand.nextInt(32, 128);
//
//		long result = 0;
//
//		for (int i = 0; i < count; i++) {
//			result += rand.nextInt();
//		}
//
//		System.out.println(result);
//		System.out.println(result % 24);
//		System.out.println(rand.nextBoolean());

		Lock[] stick = {new ReentrantLock(), new ReentrantLock(), new ReentrantLock(), new ReentrantLock(), new ReentrantLock()};
		
		Philosopher erik = new Philosopher("Erik", stick[0], stick[1], true);
		Philosopher dan = new Philosopher("Dan", stick[1], stick[2], true);
		Philosopher kiril = new Philosopher("Kiril", stick[2], stick[3], true);
		Philosopher vova = new Philosopher("Vova", stick[3], stick[4], true);
		Philosopher anton = new Philosopher("Anton", stick[4], stick[0], true);
		
		new Thread(erik).start();
		new Thread(dan).start();
		new Thread(kiril).start();
		new Thread(vova).start();
		new Thread(anton).start();
		
		
	}

	static class Philosopher implements Runnable {
		private String name;
		private Lock left;
		private Lock right;
		private Boolean leftHanded;

		public Philosopher(String name, Lock left, Lock right, Boolean lh) {
			super();
			this.name = name;
			this.left = left;
			this.right = right;
			this.leftHanded = lh;
		}

		@Override
		public void run() {

			while (!Thread.currentThread().isInterrupted()) {

				if (left.tryLock()) {
					if (right.tryLock()) {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("Philosopher " + name + " end to eating!");
						left.unlock();
						right.unlock();
					}
					else {
						left.unlock();
					}
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

	}

}
