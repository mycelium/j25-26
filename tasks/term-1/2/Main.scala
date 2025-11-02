package recfun

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("\nParentheses Balancing")
    val exprs = List("()", "(())", "())(", "(()", "())", "((a + b) * c)")
    for (s <- exprs) {
      println(s + ": " + balance(s.toList))
    }

    println("\nCounting Change")
    println("countChange(4, List(1,2)): " + countChange(4, List(1,2)))
    println("countChange(5, List(2,3)): " + countChange(5, List(2,3)))
    println("countChange(10, List(2,5,3,6)): " + countChange(10, List(2,5,3,6)))

    println("\nN-Queens Solution (size = 4):")
    nQueens(4) match {
      case Some(solution) =>
        // печать доски
        for (row <- 0 until 4) {
          for (col <- 0 until 4)
            print(if (solution(row) == col) "1 " else "0 ")
          println()
        }
        println("Columns: " + solution.mkString("[", ", ", "]"))
      case None => println("No solution")
    }
  }



  // Exercise 1: Элемент треугольника Паскаля
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }


  // Exercise 2 Parentheses Balancing
  def balance(chars: List[Char]): Boolean = {
    def loop(chars: List[Char], open: Int): Boolean = {
      if (chars.isEmpty) open == 0
      else if (open < 0) false
      else if (chars.head == '(') loop(chars.tail, open + 1)
      else if (chars.head == ')') loop(chars.tail, open - 1)
      else loop(chars.tail, open)
    }
    loop(chars, 0)
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
          queens(i) + i == col + row) return false
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

    val queens = new Array[Int](size)
    if (solve(0, queens)) Some(queens) else None
  }
}


