package ru.spbstu.hsai.java.l1008;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

public class MainNIO {
	public static void main(String[] args) {
		File file = new File("");

		Path path = Path.of("");
		
		try {
			ReadableByteChannel fileChannel = Channels.newChannel(new FileInputStream(file));

			ByteBuffer bb;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	
}
