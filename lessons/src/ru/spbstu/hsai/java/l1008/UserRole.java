package ru.spbstu.hsai.java.l1008;

public enum UserRole {
	
	GUEST(1),
	USER(2),
	ADMIN(3);
	
	private int id;
	
	private UserRole(int roleId) {
		this.id=roleId;
	}
	
	public int getId() {
		return this.id;
	}
}
