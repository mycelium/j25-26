package ru.spbstu.hsai.java.l1022;

import java.util.concurrent.Callable;

public class Main {
	
	private String nonStaticData = "";
	
	private static String data = "Hello";
	
	public static void main(String[] args) {
		Function<String, Integer> strToInt = new Function<String, Integer>() {

			@Override
			public Integer fun(String parameter) {
				return Integer.parseInt(parameter);
			}
		};
		strToInt.fun("");
		
		Runnable runnable = () -> {
			
			System.out.print("Hello World");
			
		};
		
		
		var string = "Hello World";
		
		Callable<String> callable = () -> {		
			return data;
		};
		
//		var x = () -> {};
		

	}
	
	public void doSmth() {
		Callable<String> dosmth = () -> {
			return nonStaticData;
			
		};
	}
}
