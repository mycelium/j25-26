package recfun

object Main {
  def main(args: Array[String]): Unit = {
    println(" Pascal's Triangle ")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("\n")
    println(" Parentheses Balancing Tests")
    val test1 = "(if (zero? x) max (/ 1 x))".toList
    val test2 = "I told him (that it's not (yet) done).\n(But he wasn't listening)".toList
    val test3 = ":-)".toList
    val test4 = "())(".toList

    println(s"Test 1: ${balance(test1)}") // true
    println(s"Test 2: ${balance(test2)}") // true
    println(s"Test 3: ${balance(test3)}") // false
    println(s"Test 4: ${balance(test4)}") // false

    println("\n=== Counting Change Tests ===")
    println(s"countChange(4, List(1,2)) = ${countChange(4, List(1,2))}") // 3
    println(s"countChange(5, List(2,3)) = ${countChange(5, List(2,3))}") // 1
    println(s"countChange(10, List(1,2,5)) = ${countChange(10, List(1,2,5))}") // 10

    println("\n=== N-Queens Problem ===")
    println("Solution for 4 queens:")
    nQueens(4) match {
      case Some(solution) => printBoard(solution)
      case None => println("No solution found")
    }

    println("\nSolution for 8 queens:")
    nQueens(8) match {
      case Some(solution) =>
        println("Board positions: " + solution.mkString("[", ", ", "]"))
        printBoard(solution)
      case None => println("No solution found")
    }
  }

  /**
   * Exercise 1: Pascal's Triangle
   * Returns the value at column c and row r in Pascal's triangle
   */
  def pascal(c: Int, r: Int): Int = {
    if (c < 0 || c > r) 0
    else if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /**
   * Exercise 2: Parentheses Balancing
   * Checks if the parentheses in a character list are balanced
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
   * Exercise 3: Counting Change
   * Counts how many different ways you can make change for an amount
   * given a list of coin denominations
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) 1
    else if (money < 0 || coins.isEmpty) 0
    else {
      countChange(money, coins.tail) + countChange(money - coins.head, coins)
    }
  }

  /**
   * Exercise 4: N-Queens Problem
   * Finds a solution to the n-queens problem using backtracking
   * Returns an array where index represents row and value represents column
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

    val queens = new Array[Int](size)
    if (solve(queens, 0)) Some(queens) else None
  }

  /**
   * Helper function to print the chess board
   */
  def printBoard(solution: Array[Int]): Unit = {
    val size = solution.length
    for (i <- 0 until size) {
      for (j <- 0 until size) {
        if (solution(i) == j) print("Q ") else print(". ")
      }
      println()
    }
    println(s"Solution: ${solution.mkString("[", ", ", "]")}")
  }
}