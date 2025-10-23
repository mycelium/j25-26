package recfun
// import common._

object Main {
  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("\n" + balance("((qw()ewqew))".toList))
    println("\n" + balance("((qw()ewqew".toList))

    println("\n" + countChange(10, List(5, 1)))

    
    val result = nQueens(4)
    result match {
      case Some(solution) =>
        println("Solution found:")
        printBoard(solution)
      case None =>
        println("No solution found")
    }


  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || c == r) 1
    else pascal(c - 1, r -1) + pascal(c, r - 1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    def help(chars: List[Char], count: Int): Boolean = {
      if (chars.isEmpty) {
        count == 0
      } else {
        val currentChar = chars.head
        val newCount = currentChar match {
          case '(' => count + 1
          case ')' => count - 1
          case _   => count
        }
        if (newCount < 0) false
        else help(chars.tail, newCount)
      }
    }
    help(chars, 0)
  }

  /**
   * Exercise 3 Counting Change
   * Write a recursive function that counts how many different ways you can make
   * change for an amount, given a list of coin denominations. For example,
   * there is 1 way to give change for 5 if you have coins with denomination
   * 2 and 3: 2+3.
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money < 0) 0
    else if (coins.isEmpty) 0
    else if (money == 0) 1
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
        val c = queens(r)
        if (c == col || (row - r).abs == (col - c).abs) return false
      }
      true
    }

    def solve(row: Int, queens: Array[Int]): Option[Array[Int]] = {
      if (row == size) return Some(queens.clone())
      for (col <- 0 until size) {
        if (isSafe(queens, row, col)) {
          queens(row) = col
          solve(row + 1, queens) match {
            case Some(solution) => return Some(solution)
            case None =>
          }
        }
      }
      None
    }
    solve(0, new Array[Int](size))
  }
  def printBoard(solution: Array[Int]): Unit = {
    val n = solution.length
    for (row <- 0 until n) {
      for (col <- 0 until n) {
        if (solution(row) == col) print("1 ") else print("0 ")
      }
      println()
    }
  }
}
