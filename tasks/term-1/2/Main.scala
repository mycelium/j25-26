package recfun
import common._

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("\nTesting balance:")
    println(balance("(just (an) example (for (task 2)))".toList)) // true
    println(balance("())p(".toList)) // false
    println(balance("".toList)) // true

    println("\nTesting countChange:")
    println(countChange(4, List(1,2))) // 3
    println(countChange(5, List(2,3))) // 1

    println("\nTesting nQueens:")
    testNQueens(4)
    testNQueens(8)
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if (c==0 || c==r) 1
    else if (c<0 || r<0 || c>r) 0
    else pascal(c-1, r-1) + pascal(c, r-1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    def balanceHelper(chars: List[Char], openCount: Int): Boolean = {
      if (chars.isEmpty) openCount == 0
      else if (openCount < 0) false
      else chars.head match {
        case '(' => balanceHelper(chars.tail, openCount + 1)
        case ')' => balanceHelper(chars.tail, openCount - 1)
        case _ => balanceHelper(chars.tail, openCount)
      }
    }
    balanceHelper(chars, 0)
  }

  /**
   * Exercise 3 Counting Change
   * Write a recursive function that counts how many different ways you can make
   * change for an amount, given a list of coin denominations. For example,
   * there is 1 way to give change for 5 if you have coins with denomiation
   * 2 and 3: 2+3.
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    if(money == 0) 1
    else if (money < 0 || coins.isEmpty) 0
    else {
      countChange(money - coins.head, coins) + countChange(money, coins.tail)
    }
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
    def isSafe(board: Array[Int], row: Int, col: Int): Boolean = {
      for (i <- 0 until row) {
        // Проверяем тот же столбец или диагонали
        if (board(i) == col ||
          math.abs(board(i) - col) == math.abs(i - row)) {
          return false
        }
      }
      true
    }

    def solve(row: Int, board: Array[Int]): Boolean = {
      if (row == size) true
      else {
        for (col <- 0 until size) {
          if (isSafe(board, row, col)) {
            board(row) = col
            if (solve(row + 1, board)) {
              return true
            }
          }
        }
        false
      }
    }

    val board = Array.fill(size)(-1)
    if (solve(0, board)) Some(board) else None
  }

  def printBoard(queens: Array[Int]): Unit = {
    val size = queens.length
    for (i <- 0 until size) {
      for (j <- 0 until size) {
        if (queens(i) == j) print("1 ") else print("0 ")
      }
      println()
    }
    println()
  }

  def testNQueens(size: Int): Unit = {
    println(s"\nSolutions for board size $size:")
    val solution = nQueens(size)
    solution match {
      case Some(arr) =>
        println(s"Found solution: ${arr.mkString("[", ",", "]")}")
        printBoard(arr)
      case None =>
        println("No solution found")
    }
  }
}