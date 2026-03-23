package ru.spbstu.hsai.java.l0205;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class Request implements Serializable {

	private byte[] imageBytes;
	private User user;

	public Request(byte[] imageBytes) {
		super();
		this.imageBytes = imageBytes;
	}

	public Request(byte[] imageBytes, User user) {
		super();
		this.imageBytes = imageBytes;
		this.user = user;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}
	
	public String getUsers() {
		return user.toString();
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}

}
