package recfun

object Main {
  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
    println(s"Баланс скобок: ${balance("())() ".toList)}")
    println(s"Количество способов размена для 10 с монетами [1, 2, 3, 4, 5]: ${countChange(10, List(1, 2, 3, 4, 5))}")
    println(s"Количество способов размена для 5 с монетами [2, 3]: ${countChange(5, List(2, 3))}")
    val solutions = List(4, 5, 6, 8)
    for (size <- solutions) {
      println(s"Решение ${size}x${size}:")
      nQueens(size) match {
        case Some(queens) =>
          println(s"Расстановка: ${queens.mkString("[", ", ", "]")}")
          println("Доска:")
          printBoard(queens)
          println() // пустая строка после доски
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
    if (c < 0 || c > r) 0
    else if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
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
      val colDiff = math.abs(queens(i) - col)
      val rowDiff = row - i
      if (queens(i) == col || colDiff == rowDiff) {
        return false
      }
    }
    true
  }

  def solve(queens: Array[Int], currentRow: Int): Boolean = {
    if (currentRow == size) true
    else {
      for (col <- 0 until size) {
        if (isSafe(queens, currentRow, col)) {
          queens(currentRow) = col
          if (solve(queens, currentRow + 1)) {
            return true
          }
        }
      }
      false
    }
  }

  val queens = Array.fill(size)(-1)
  if (solve(queens, 0)) Some(queens)
  else None
}
def printBoard(solution: Array[Int]): Unit = {
    val size = solution.length
    for (row <- 0 until size) {
      for (col <- 0 until size) {
        // 1 если в этой клетке ферзь, иначе 0
        if (solution(row) == col) print("1 ") 
        else print("0 ")
      }
      println()
    }
  }
}

