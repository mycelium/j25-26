package ru.spbstu.hsai.java.l1126;


public class WaitNot {

	private final Object monitor = new Object();

	private StringBuilder resource = new StringBuilder();

	private class Producer implements Runnable {

		public boolean keepRunning = true;

		@Override
		public void run() {
			while (keepRunning) {
				synchronized (monitor) {
					resource.append("a");
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (resource.length()>-32) {
						monitor.notify();
					}
				}
			}

		}

	}

	private class Consumer implements Runnable {

		public boolean keepRunning = true;

		@Override
		public void run() {
			while (keepRunning) {
				
				synchronized (monitor) {
					if (resource.length()<32) {
						try {
							monitor.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}else {
						System.out.println(resource.toString());
						resource.setLength(0);
					}
				}
			}

		}

	}
	
	public void start() {
		Consumer consumer = new Consumer();
		Producer producer1 = new Producer();
		Producer producer2 = new Producer();
		
		new Thread(consumer).start();
		new Thread(producer1).start();
		new Thread(producer2).start();
		
		Thread thr = new Thread();
		
		thr.interrupt();
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		consumer.keepRunning=false;
		producer1.keepRunning=false;
		producer2.keepRunning=false;
		
	}
	
	public static void main(String[] args) {
		new WaitNot().start();
	}
}
