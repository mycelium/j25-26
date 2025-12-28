package recfun

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("\nBalance tests:")
    println(balance("(if (zero? x) max (/ 1 x))".toList))
    println(balance("I told him (that it's not (yet) done). (But he wasn't listening)".toList))
    println(balance(":-)".toList))
    println(balance("())(".toList))

    println("\nCount Change tests:")
    println(s"countChange(4, List(1,2)): ${countChange(4, List(1,2))}")
    println(s"countChange(5, List(2,3)): ${countChange(5, List(2,3))}")
    println(s"countChange(10, List(1,5,10)): ${countChange(10, List(1,5,10))}")
    println(s"countChange(0, List(1,2)): ${countChange(0, List(1,2))}")
    println(s"countChange(5, List()): ${countChange(5, List())}")
    println(s"countChange(-5, List(1,2)): ${countChange(-5, List(1,2))}")

    println("\nN-Queens for size 4:")
    nQueens(4) match {
      case Some(solution) => printBoard(solution)
      case None => println("No solution")
    }
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if(c < 0 || r < 0 || c > r) 0
    else if(c == 0 || c == r) 1
    else pascal(c-1,r-1) + pascal(c,r-1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    def check(cs: List[Char], open: Int): Boolean = {
      if(open < 0) false
      else if(cs.isEmpty) open == 0
      else if(cs.head == '(') check(cs.tail, open + 1)
      else if(cs.head == ')') check(cs.tail, open - 1)
      else check(cs.tail, open)
    }
    check(chars, 0)
  }

  /**
   * Exercise 3 Counting Change
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) 1
    else if (money < 0 || coins.isEmpty) 0
    else if (coins.head <= 0) countChange(money, coins.tail)
    else countChange(money - coins.head, coins) + countChange(money, coins.tail)
  }

  /**
   * Exercise 4 N-Queens Problem
   */
  def nQueens(size: Int): Option[Array[Int]] = {
    if(size <= 0) return None

    def isSafe(board: List[Int], row: Int, col: Int): Boolean = {
      board.zipWithIndex.forall { case (c, r) =>
        c != col && math.abs(c - col) != math.abs(r - row)
      }
    }

    def solve(row: Int, board: List[Int]): Option[List[Int]] = {
      if(row == size) Some(board)
      else {
        (0 until size).flatMap { col =>
          if(isSafe(board, row, col)) solve(row + 1, board :+ col)
          else None
        }.headOption
      }
    }

    solve(0, List()).map(_.toArray)
  }

  def printBoard(solution: Array[Int]): Unit = {
    println("Solution: " + solution.mkString("[", ",", "]"))
    for(row <- solution.indices) {
      for(col <- solution.indices) {
        if(solution(row) == col) print("1 ")
        else print("0 ")
      }
      println()
    }
  }
}