package ru.spbstu.hsai.java.l1015;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class IOExperiment {

	// TODO write 4 variants of copying file:
	// - IO with Input\Output Stream
	// - NIO with FileChannel
	// - NIO with FileChannel and ByteBuffer
	// - NIO with FIles
	// Measure time

	// IO with Input\Output Stream
	private static void copyFileUsingStreams(Path source, Path dest) {
		try (InputStream input = new FileInputStream(source.toAbsolutePath().toString());
				OutputStream output = new FileOutputStream(dest.toAbsolutePath().toString())) {

			byte[] chunk = new byte[4096];

			int byteCount;
			while ((byteCount = input.read(chunk)) != -1) {
				output.write(chunk);
			}

		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	// - NIO with FileChannel
	private static void copyFileUsingFileChannel(Path source, Path dest) {
		try (FileChannel input = FileChannel.open(source, StandardOpenOption.READ);
				FileChannel output = FileChannel.open(dest, StandardOpenOption.CREATE, StandardOpenOption.WRITE);) {
			long result = input.transferTo(0, input.size(), output);
			if (result != input.size()) {
				System.out.println("Не получилось ;(");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void copyFIleUsingByteBuffer(Path source, Path dest) {

		try (FileChannel input = FileChannel.open(source, StandardOpenOption.READ);
				FileChannel output = FileChannel.open(dest, StandardOpenOption.CREATE, StandardOpenOption.WRITE);) {

			ByteBuffer bb = ByteBuffer.allocate(4096);

			while (true) {
				int check = input.read(bb);
				if (check <= 0) {
					break;
				}
				bb.flip();
				output.write(bb);
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	private static void copyFile(Path source, Path dest) {
		try {
			Files.copy(source, dest);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public static void main(String[] args) {

		// Random rand = new Random();
		// System.out.println(rand.nextInt(24)+1);
		// System.out.println(rand.nextDouble()>=0.5 ? "right": "left");

		Path source = Path.of("source/heavy.mkv");
		Path sink = Path.of("sink/heavyCopy.mkv");

		long time = System.currentTimeMillis();
		copyFile(source, sink);
		// withFileChannel(source, sink);
		System.out.println(System.currentTimeMillis() - time);
	}
}
