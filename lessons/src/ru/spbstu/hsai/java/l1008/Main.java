package ru.spbstu.hsai.java.l1008;

import static ru.spbstu.hsai.java.l1008.UserRole.ADMIN;

public class Main {

	private enum Test {

	}

	public static void main(String[] args) {

		enum TestTest {
			TEST;
		}

		TaskType type = TaskType.JAVA;

		var java = TaskType.valueOf("JAVA");

		System.out.println(java);

		System.out.println(type == TaskType.JAVA);

		System.out.println(ADMIN.getId());

		var x = ADMIN;
	}
}
