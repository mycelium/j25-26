package recfun


object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }


    println("\nParentheses Balancing:")
    println("please ((work this code)))");
    println(balance("please (((work this code)))".toList))
    println("nice vrode work))(())((");
    println(balance("nice vrode work))(())((".toList))


    println("\nCounting Change:")
    println("ways to 5 from 1 and 2")
    println(countChange(5, List(1,2)))
    println("ways to 5 from 2 and 2")
    println(countChange(5, List(2,2)))
    println("ways to 5 from 3 and 2")
    println(countChange(5, List(3,2)))


    println("\nN-Queens Problem:")
    println("\n4 queens")
    nQueens(4) match {
      case Some(solution) =>
        println("Board")
        printBoard(solution)
      case None => println("No solution found")
    }
    println("\n10 queens")
    nQueens(10) match {
      case Some(solution) =>
        println("Board")
        printBoard(solution)
      case None => println("No solution found")
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
    def balanceRecurse(chars: List[Char], openCount: Int): Boolean = {
      if (chars.isEmpty) openCount == 0
      else if (openCount < 0) false
      else chars.head match {
        case '(' => balanceRecurse(chars.tail, openCount + 1)
        case ')' => balanceRecurse(chars.tail, openCount - 1)
        case _ => balanceRecurse(chars.tail, openCount)
      }
    }

    balanceRecurse(chars, 0)
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
      for (i <- 0 until row) {
        if (queens(i) == col ||
          queens(i) - i == col - row ||
          queens(i) + i == col + row) {
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

  def printBoard(queens: Array[Int]): Unit = {
    val size = queens.length
    for (i <- 0 until size) {
      for (j <- 0 until size) {
        if (queens(i) == j) print("1 ") else print("0 ")
      }
      println()
    }
  }
}
