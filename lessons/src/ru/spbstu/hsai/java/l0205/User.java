package ru.spbstu.hsai.java.l0205;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.stream.Stream;

public class User implements Serializable {
	private String name;
	private String login;
	private String password;
	private static String usersDB = "users.txt";
	private static FileWriter file = null;

	public User(String name, String login, String password) {

		boolean userInSystem = usersCheking(name, login, password);

		if (!userInSystem) {
			this.name = name;
			this.login = login;
			this.password = password;

			this.writeToFile();
		}

		else {

		}
	}

	public boolean usersCheking(String name, String login, String password) {

		try (BufferedReader br = new BufferedReader(new FileReader(usersDB))) {
			// Stream<String> line = br.lines();
			String line;

			while ((line = br.readLine()) != null) {
				String[] us = line.strip().split(" ");
				System.out.println(us[0]+us[1]+us[2]);
				if (us[0].equals(name) && us[1].equals(login) && us[2].equals(password)) {
					return true;
				}
			}
		} catch (Exception e) {
			System.out.println("File exeption!");
		}
		return false;
	}


	public String getName() {
		return name;
	}

	public void writeToFile() {
		try {
			file = new FileWriter(usersDB, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			file.append(this.toString());
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String toString() {
		String s = name + " " + login + " " + password + "\n";
		return s;
	}

}
