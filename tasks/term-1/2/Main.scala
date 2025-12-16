package recfun

object Main {
  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
    
    // Тестирование баланса скобок
    println("\nBalance test:")
    println(balance("(just an) example".toList)) // true
    println(balance("())(".toList)) // false
    
    // Тестирование размена денег
    println("\nCount change test:")
    println(countChange(5, List(2, 3))) // 1
    
    // Тестирование N-Queens
    println("\nN-Queens test:")
    nQueens(4) match {
      case Some(solution) => printBoard(solution)
      case None => println("No solution found")
    }
  }

  /**
   * Exercise 1 - Triangle Pascal
   */
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /**
   * Exercise 2 - Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    def balanceHelper(chars: List[Char], count: Int): Boolean = {
      if (chars.isEmpty) count == 0
      else if (count < 0) false
      else chars.head match {
        case '(' => balanceHelper(chars.tail, count + 1)
        case ')' => balanceHelper(chars.tail, count - 1)
        case _ => balanceHelper(chars.tail, count)
      }
    }
    balanceHelper(chars, 0)
  }

  /**
   * Exercise 3 - Counting Change
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) 1
    else if (money < 0 || coins.isEmpty) 0
    else countChange(money - coins.head, coins) + countChange(money, coins.tail)
  }
  
  /**
   * Exercise 4 - N-Queens Problem
   */
  def nQueens(size: Int): Option[Array[Int]] = {
    def isSafe(queens: Array[Int], row: Int, col: Int): Boolean = {
      (0 until row).forall { i =>
        queens(i) != col && 
        math.abs(queens(i) - col) != row - i
      }
    }
    
    def solve(queens: Array[Int], row: Int): Boolean = {
      if (row == size) true
      else {
        (0 until size).exists { col =>
          if (isSafe(queens, row, col)) {
            queens(row) = col
            if (solve(queens, row + 1)) true
            else {
              queens(row) = -1
              false
            }
          } else false
        }
      }
    }
    
    val queens = Array.fill(size)(-1)
    if (solve(queens, 0)) Some(queens) else None
  }
  
  /**
   * Печать доски для N-Queens
   */
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