package ru.spbstu.hsai.java.exam;

public class Pluchin {
	
	private static abstract class Purachse{
		
		abstract int payemnt();
		abstract void bonus(int price);
		abstract void shippment();
		
		public void processPayment() {
			System.out.println("Start processing payment");
			var price = payemnt();
			System.out.println("Payment prccessed with "+ price);
			bonus(price);
			System.out.println("Process shippment");
			shippment();
			System.out.println("Shipped");
		}
		
	}
	
	public static void main(String[] args) {
		
	}
	
}

