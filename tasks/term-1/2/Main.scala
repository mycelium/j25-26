object Main {
  def main(args: Array[String]) {
    println("Task 1. Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("\nTask 2. Balance")
    val first: String = "(((bal)ance)(()test)())"
    println(s"$first - " + balance(first.toList))
    val second: String = ")("
    println(s"$second - " + balance(second.toList))

    println("\nTask 3. Counting Change")
    println("'countChange(4, List(1, 2))' = " + countChange(4, List(1, 2)))
    println("'countChange(5, List(2, 3))' = " + countChange(5, List(2, 3)))
    println("'countChange(0, List(1, 2, 3, 4, 5, 6, 7, 8))' = " + countChange(0, List(1, 2, 3, 4, 5, 6, 7, 8)))
    println("'countChange(4, List(3))' = " + countChange(4, List(3)))

    println("\nTask 4. N-Queens")
    for (boardSize <- 3 to 5) {
      println(s"Board size: $boardSize")
      nQueens(boardSize) match {
        case Some(solution) =>
          println("Solution: " + solution.mkString("[", ", ", "]"))
          printBoard(solution)
          println()
        case None => println("No solution found\n")
      }
    }
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if (c < 0 || c > r) 0
    else if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
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
    if (money == 0) 1
    else if (coins.isEmpty || money < 0) 0
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
    def isSafe(queens: Array[Int], currentRow: Int, currentColumn: Int): Boolean = {
      for (prevRow <- 0 until currentRow) {
        val prevColumn = queens(prevRow)
        if (prevColumn  == currentColumn || math.abs(currentColumn - prevColumn) == math.abs(currentRow - prevRow)) return false
      }
      true
    }

    def placeQueens(row: Int, queens: Array[Int]): Boolean = {
      if (row == size) return true

      for (col <- 0 until size) {
            if (isSafe(queens, row, col)) {
                queens(row) = col
                if (placeQueens(row + 1, queens))
                    return true
            }
        }
        false
    }

    val queens = Array.fill(size)(-1)
    if (placeQueens(0, queens)) Some(queens) else None
  }

  def printBoard(solution: Array[Int]): Unit = {
    for (row <- solution.indices) {
      for (col <- solution.indices) {
        if (solution(row) == col) print("1 ") else print("0 ")
      }
      println()
    }
  }
}
