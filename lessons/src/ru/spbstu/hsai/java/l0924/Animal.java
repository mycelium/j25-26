package ru.spbstu.hsai.java.l0924;

public abstract class Animal implements Feed {
	
	public abstract void breath();
	
	public void live() {
		
		System.out.println("need to breath");
		breath();
		
		drink();
		
		eat(null);
	}
}
