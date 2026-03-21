package ru.spbstu.hsai.java.l1119;

public class Task implements Runnable {

	private int value = 42;

	private String name;

	private String result;

	public Task(int value, String name) {
		super();
		this.value = value;
		this.name = name;
	}

	@Override
	public void run() {
		System.out.println("Hello World");
	}

	public String getResult() {
		return result;
	}

}
