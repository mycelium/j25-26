package recfun

import scala.compiletime.ops.int

object Main {
  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println(s"Баланс скобок: ${balance("(just an)) example".toList)}")

    println(s"Количество сдач для 10 и монет 1 2 3 4 5: ${countChange(10, List(1, 2, 3, 4, 5))}")

    val solutions = List(4, 5, 6, 8)

    for (size <- solutions) {
      println(s"Решение для доски $size x $size:")
      nQueens(size) match {
        case Some(queens) =>
          println(s"Расстановка: ${queens.mkString("[", ", ", "]")}")
          printBoard(queens)
        case None =>
          println("Решение не найдено")
          println()
      }
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
    def helper(chars: List[Char], count: Int): Boolean = {
      if (chars.isEmpty) count == 0
      else if (count < 0) false
      else{
        val current = chars.head
        val newCount = current match
          case '(' => count + 1
          case ')' => count - 1
          case _ => count
        helper(chars.tail, newCount)
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
        if (queens(i) == col || 
          math.abs(queens(i) - col) == math.abs(i - row)) {
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
    
    val queens = Array.fill(size)(-1)
    if (solve(queens, 0)) Some(queens)
    else None
    }

}

def printBoard(queens: Array[Int]): Unit = {
  val size = queens.length
  for (i <- 0 until size) {
    for (j <- 0 until size) {
      if (queens(i) == j) print("1 ")
      else print("0 ")
    }
    println()
  }
  println()
}