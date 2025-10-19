package recfun
import common._

object Main {
  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || c == r) 1
    else if (c < 0 || c > r) 0
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    var counter = 0;
    for (char <- chars) {
      char match {
        case '(' => counter += 1
        case ')' => counter -= 1
        case _ =>
      }
    }
    counter == 0
  }

  /**
   * Exercise 3 Counting Change
   * Write a recursive function that counts how many different ways you can make
   * change for an amount, given a list of coin denominations. For example,
   * there is 1 way to give change for 5 if you have coins with denomiation
   * 2 and 3: 2+3.
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) 1
    else if (money < 0) 0
    else if (coins.isEmpty) 0
    else countChange(money - coins.head, coins) + countChange(money, coins.tail)
  }
  
  /**
   * Excerice 4 N-Queens Problem
   * Write a function that provides a solution for n-queens problem if possible
   * Input parameter represents board size and number of queens
   * Output is an array: index represents row number and value represents column number.
   * Example: nQueens(4): [1,3,0,2] or
   * 0 1 0 0
   * 0 0 0 1
   * 1 0 0 0
   * 0 0 1 0
   */

  def nQueens(size: Int): Option[Array[Int]] = {
  def isSafe(queens: Array[Int], row: Int, col: Int): Boolean = {
    for (i <- 0 until row) {
      if (queens(i) == col || math.abs(queens(i) - col) == math.abs(i - row)) {
        return false
      }
    }
    true
  }
  
  def solve(queens: Array[Int], row: Int): Boolean = {
    if (row == size) true
    else {
      for (col <- 0 until size) {
        if (isSafe(queens, row, col)) {
          queens(row) = col
          if (solve(queens, row + 1)) return true
        }
      }
      false
    }
  }
  
  val queens = new Array[Int](size)
  if (solve(queens, 0)) Some(queens) else None
}

def printBoard(queens: Array[Int]): Unit = {
  val size = queens.length
  
  for (row <- 0 until size) {
    for (col <- 0 until size) {
      if (queens(row) == col) print("1 ") 
      else print("0 ")
    }
    println()
  }
  println()
  

}
