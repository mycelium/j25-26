package recfun

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("\n2. Parentheses Balance Check")
    val testString = "((())())"
    println(s"Testing string: '$testString'")
    println(s"Is balanced: ${balance(testString.toList)}")

    println("\n3. Counting Change Combinations")
    val amount = 6
    val coinValues = List(1, 2, 4)
    println(s"Amount: $amount, coins: $coinValues")
    println(s"Ways to make change: ${countChange(amount, coinValues)}")

    println("\n4. N-Queens Problem")
    val n = 8
    println("Solution for N = " + n + ":")
    nQueens(n) match {
      case Some(solution) =>
        println("Queen positions: " + solution.mkString("[", ", ", "]"))
        printBoard(solution)
      case None => println("No solution found for N = " + n)
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
    def recCheck(chars: List[Char], total: Int): Boolean = {
      if (chars.isEmpty) total == 0
      else if (total < 0) false
      else {
        val current = chars.head
        val newTotal =
          if (current == '(') total + 1
          else if (current == ')') total - 1
          else total
        recCheck(chars.tail, newTotal)
      }
    }
    recCheck(chars, 0)
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
    def isSafe(queenPositions: Array[Int], currentRow: Int, currentColumn: Int): Boolean = {
      for (previousRow <- 0 until currentRow) {
        val previousColumn = queenPositions(previousRow)
        if (previousColumn == currentColumn || math.abs(currentColumn - previousColumn) == math.abs(currentRow - previousRow)) return false
      }
      true
    }

    def placeQueens(row: Int, queenPositions: Array[Int]): Boolean = {
      if (row == size) return true

      for (column <- 0 until size) {
        if (isSafe(queenPositions, row, column)) {
          queenPositions(row) = column
          if (placeQueens(row + 1, queenPositions))
            return true
        }
      }
      false
    }

    val queenPositions = Array.fill(size)(-1)
    if (placeQueens(0, queenPositions)) Some(queenPositions) else None
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