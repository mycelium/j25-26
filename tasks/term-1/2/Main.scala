package recfun

object Main {

  def main(args: Array[String]): Unit = {


    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    val parenthesesTest = "((())())"
    println(s"Balanced parentheses for '$parenthesesTest': ${balance(parenthesesTest.toList)}")

   
    val changeTest1 = 10
    val coinsTest1 = List(1, 2, 5)
    println(s"Number of ways to make change for $changeTest1 with coins $coinsTest1: ${countChange(changeTest1, coinsTest1)}")

    val changeTest2 = 7
    val coinsTest2 = List(3, 4)
    println(s"Number of ways to make change for $changeTest2 with coins $coinsTest2: ${countChange(changeTest2, coinsTest2)}")

   
    val sizes = List(4, 5, 6)
    for (size <- sizes) {
      println(s"Solution for a ${size}x${size} board:")
      nQueens(size) match {
        case Some(queens) =>
          println(s"Placement: ${queens.mkString(", ")}")
          printBoard(queens)
          println() 
        case None =>
          println("No solution found.")
          println()
      }
    }
  }

  /** Exercise 1 */
  def pascal(c: Int, r: Int): Int = {
    if (c < 0 || c > r) 0
    else if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /** Exercise 2 Parentheses Balancing */
  def balance(chars: List[Char]): Boolean = {
    def helper(chars: List[Char], count: Int): Boolean = {
      if (chars.isEmpty) count == 0
      else if (count < 0) false
      else {
        val newCount = chars.head match {
          case '(' => count + 1
          case ')' => count - 1
          case _ => count
        }
        helper(chars.tail, newCount)
      }
    }
    helper(chars, 0)
  }

  /** Exercise 3 Counting Change */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) 1
    else if (money < 0 || coins.isEmpty) 0
    else countChange(money - coins.head, coins) + countChange(money, coins.tail)
  }

  /** Exercise 4 N-Queens Problem */
  def nQueens(size: Int): Option[Array[Int]] = {
    def isSafe(queens: Array[Int], row: Int, col: Int): Boolean = {
      for (i <- 0 until row) {
        val colDiff = math.abs(queens(i) - col)
        val rowDiff = row - i
        if (queens(i) == col || colDiff == rowDiff) return false
      }
      true
    }

    def solve(queens: Array[Int], currentRow: Int): Boolean = {
      if (currentRow == size) true
      else {
        for (col <- 0 until size) {
          if (isSafe(queens, currentRow, col)) {
            queens(currentRow) = col
            if (solve(queens, currentRow + 1)) return true
          }
        }
        false
      }
    }

    val queens = Array.fill(size)(-1)
    if (solve(queens, 0)) Some(queens) else None
  }

  def printBoard(solution: Array[Int]): Unit = {
    val size = solution.length
    for (row <- 0 until size) {
      for (col <- 0 until size) {
        if (solution(row) == col) print("Q ") 
        else print(". ")  
      }
      println()
    }
  }
}
