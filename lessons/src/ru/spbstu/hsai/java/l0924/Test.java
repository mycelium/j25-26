package ru.spbstu.hsai.java.l0924;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

public class Test {
	
	record Cat() {};
	
	
	public static void main(String[] args) {
		Main main = new Main(1);
		
		
		int intParam = 16;
		Integer  integerParam = intParam;
		intParam = integerParam;
		
		Integer int1 = 255;
		Integer int2 = 255;
		System.out.println(int1 == int2);
		
		Integer int3 = Integer.valueOf(127);
		Integer int4 = Integer.valueOf(127);
		
		System.out.println(int3 == int4);

		Integer int5 = new Integer(127);
		Integer int6 = new Integer(127);
		
		System.out.println(int5 == int6);
		
//		int int7;
//		Integer int8 = null;
//		
//		int7 = int8;
		
		String str1 = "Hello world";
		String str2 = """
				"var": 1;
				""";
		String str3 = "Hello world";
		
		System.out.println(str1.equals(str3));
		
		int[] intArr = new int[16];
		
		int[][] intMatrix = new int[16][8];
		
		long startTime = System.currentTimeMillis();
		doSmth();
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime);
		
		Animal[] animalArr = new Animal[5];
		Croc[] crocs = new Croc[5];
		
		animalArr[0] = new Croc();
		
		Animal zebra = new Zebra();
		
		animalArr[1] = zebra;
		
		Animal[] animals = crocs;
		
//		animals[3]=zebra;
		
		List<Integer> intList = new LinkedList<Integer>();
		Map<String, Integer> mapStrInt = new HashMap<String, Integer>();
		Set<String> strSet = new HashSet<>();
		
		
		
	}
	
	
	private static void doSmth() {
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
