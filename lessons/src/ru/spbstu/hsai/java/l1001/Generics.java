package ru.spbstu.hsai.java.l1001;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

public class Generics {
	public static void main(String[] args) {
		List listOb = new ArrayList();

		List<Integer> listInt = List.of();

		var x = listInt.get(0);

		Optional<String> opt = Optional.of("");

		Function<Integer, String> func = new Function<Integer, String>() {
			@Override
			public String exec(Integer param) {
				return param.toString();
			}
		};

		Collections.emptySet();
		Set.of();

		Collections.sort(listInt, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		});

		new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

			}
		};

		new Callable<Double>() {

			@Override
			public Double call() throws Exception {
				// TODO Auto-generated method stub
				return null;
			}

		};
		
		
		

	}
}
