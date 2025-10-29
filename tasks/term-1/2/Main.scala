package recfun
import common._

object Main {
  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("\nParentheses balance tests:")
    println(balance("()()".toList))       // true
    println(balance("(()".toList))        // false
    println(balance("((()))".toList))     // true
    println(balance("())(".toList))       // false

    println("\nCount change tests:")
    println(countChange(4, List(1, 2)))   // 3 (1+1+1+1, 2+1+1, 2+2)
    println(countChange(10, List(2, 5, 3, 6))) // 5

    println("\nN-Queens test:")
    val n = 8
    nQueens(n) match {
      case Some(solution) =>
        println(s"Solution for $n-Queens: " + solution.mkString("[", ", ", "]"))
        printBoard(solution)
      case None =>
        println(s"No solution found for $n-Queens.")
    }
  }

  /** Exercise 1. Pascal’s Triangle **/
  def pascal(c: Int, r: Int): Int =
    if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)

  /** Exercise 2. Parentheses Balancing **/
  def balance(chars: List[Char]): Boolean = {
    def loop(rest: List[Char], open: Int): Boolean = {
      if (open < 0) false
      else rest match {
        case Nil => open == 0
        case '(' :: tail => loop(tail, open + 1)
        case ')' :: tail => loop(tail, open - 1)
        case _ :: tail   => loop(tail, open)
      }
    }
    loop(chars, 0)
  }

  /** Exercise 3. Counting Change **/
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) 1
    else if (money < 0 || coins.isEmpty) 0
    else
      countChange(money, coins.tail) + countChange(money - coins.head, coins)
  }

  /** Exercise 4. N-Queens Problem **/
  def nQueens(size: Int): Option[Array[Int]] = {
    def isSafe(queens: Array[Int], row: Int, col: Int): Boolean = {
      for (r <- 0 until row) {
        val c = queens(r)
        if (c == col || math.abs(c - col) == math.abs(r - row))
          return false
      }
      true
    }

    def solve(row: Int, queens: Array[Int]): Boolean = {
      if (row == size) true
      else {
        for (col <- 0 until size) {
          if (isSafe(queens, row, col)) {
            queens(row) = col
            if (solve(row + 1, queens)) return true
          }
        }
        false
      }
    }

    val queens = Array.fill(size)(-1)
    if (solve(0, queens)) Some(queens) else None
  }
  def printBoard(queens: Array[Int]): Unit = {
    val size = queens.length
    for (r <- 0 until size) {
      for (c <- 0 until size) {
        if (queens(r) == c) print("1 ") else print("0 ")
      }
      println()
    }
  }
}

