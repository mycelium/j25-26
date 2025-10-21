package recfun
//import common._

object Main {
  def main(args: Array[String]): Unit = {
    println("Exercise 1 Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println()
    println("Exercise 2 Parentheses Balancing")
    println("(a(b+c) + d) + (b + c) : " + balance("(a(b+c) + d) + (b + c)".toList))
    println("(4 - (3*2))(12(5+1)) : " + balance("(4 - (3*2))(12(5+1))".toList))
    println("()) : " + balance("())".toList))
    println(")( : " + balance(")(".toList))

    println()
    println("Exercise 3 Counting Change")
    println("5 of [2,3]: " + countChange(5, List(2,3)))
    println("4 of [1,2,3]: " + countChange(4, List(1,2,3)))

    println()
    println("Excerice 4 N-Queens Problem")
    val checkSizes = List(4,5,6)
    for (size <- checkSizes){
      println("Board " + size + " x " + size + ": ")
      nQueens(size) match {
        case Some(queens) => 
          println("Queens: " + queens.mkString("[", ", ", "]"))
          printBoard(queens)
        case None => 
          println("No solutoin")
      }
      println()
    }
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    def helper(chars: List[Char], open: Int): Boolean = {
      chars match {
        case _ if (open < 0) => false
        case Nil => open == 0
        case '(' :: tail => helper(tail, open + 1)
        case ')' :: tail => helper(tail, open - 1)
        case _ :: tail => helper(tail, open)
      }
    }
    helper(chars, 0)
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
    else if (money < 0 || coins.isEmpty) 0
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
      for (r <- 0 until row) {
        if (queens(r) == col || math.abs(queens(r) - col) == math.abs(r - row)) return false
      }
      true
    }
    def place(row: Int, queens: Array[Int]): Boolean = {
      if (row == size) true
      else {
        for (col <- 0 until size) {
          if (isSafe(queens, row, col)) {
            queens(row) = col
            if (place(row + 1, queens)) return true
          }
        }
        false
      }
    }
    val queens = Array.fill(size)(-1)
    if (place(0, queens)) Some(queens) else None
  }
  def printBoard(answ: Array[Int]): Unit = {
      val n = answ.length
      for (r <- 0 until n) {
        for (c <- 0 until n) {
          if (answ(r) == c) print("1 ") else print("0 ")
        }
        println()
      }
      println()
    }
}
