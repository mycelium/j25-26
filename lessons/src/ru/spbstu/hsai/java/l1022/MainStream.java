package ru.spbstu.hsai.java.l1022;

import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MainStream {
	
	
	public static void main(String[] args) {
		var list = List.of("Apple", "Grape", "Pineapple", "Banana");
		
		var acc = new LinkedList<String>();
		
		for(String fruit : list) {
			
			if (fruit.toLowerCase().contains("apple")) {
				acc.add(fruit);
			}
		}
		
		for(String fruit : acc) {
			System.out.print(fruit + ", ");
		}
		System.out.println();
		
		var res = list.stream()
			.map(String::toLowerCase)
			.filter(x -> x.contains("apple"))
			.collect(Collectors.joining(","));
		
		System.out.println(res);

		var charNumber = list.stream().map(String::length).reduce(Integer::sum);
		System.out.println(charNumber);
		String suffix = "42";
		list.stream().map(x -> {
			System.out.println(x);
			return x+suffix;
		}).forEach(x -> System.out.println(x));
		
		
		
		
		
		
		
		
	}
	
	
	
}
