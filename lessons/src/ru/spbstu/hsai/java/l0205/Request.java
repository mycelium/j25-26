package ru.spbstu.hsai.java.l0205;

import java.io.Serializable;

public class Request implements Serializable {
	
	private byte[] imageBytes;

	public Request(byte[] imageBytes) {
		super();
		this.imageBytes = imageBytes;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}
	
}
