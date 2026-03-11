package ru.spbstu.hsai.java.l1105;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;

public class Main {
	public static void main(String[] args) {
		
		Role admin = new Role();
		admin.setId(1000);
		admin.setName("ADMIN");
		
		User user = new User();
		
		user.setId(1);
		user.setName("John");
		user.setRole(admin);
		
		Path object = Path.of("user");
		
//		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(object.toFile()))) {
//			
//			oos.writeObject(admin);
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
		
		
		Class<? extends User> clazz = user.getClass();
		
		try {
			Field field = clazz.getDeclaredField("id");
			
			field.setAccessible(true);
			field.setInt(user, -1);
			
			System.err.println(user.getId());
			
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
