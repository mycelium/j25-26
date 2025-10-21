package recfun

object Main {
  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(s"${pascal(col, row)} ")
      println()
    }

    println("\nParentheses Balancing:")
    println(balance("(:)".toList)) 
    println(balance("())(".toList)) 

    println("\nCounting Change:")
    println(countChange(5, List(1, 2, 3))) 

    println("\nN-Queens Problem:")
    nQueens(4) match {
      case Some(solution) =>
        println(s"Solution: ${solution.mkString(", ")}")
        printBoard(solution)
      case None =>
        println("No solution found.")
    }
  }

  /**
   * Exercise 1: Pascal's Triangle
   */
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /**
   * Exercise 2: Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    def loop(chars: List[Char], open: Int): Boolean = {
      if (chars.isEmpty) open == 0
      else if (chars.head == '(') loop(chars.tail, open + 1)
      else if (chars.head == ')') open > 0 && loop(chars.tail, open - 1)
      else loop(chars.tail, open)
    }
    loop(chars, 0)
  }

  /**
   * Exercise 3: Counting Change
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    def loop(money: Int, coins: List[Int]): Int = {
      if (money == 0) 1
      else if (money < 0 || coins.isEmpty) 0
      else loop(money - coins.head, coins) + loop(money, coins.tail)
    }
    loop(money, coins.sorted)
  }

  /**
   * Exercise 4: N-Queens Problem
   */
  def nQueens(size: Int): Option[Array[Int]] = {
    def isSafe(queens: Array[Int], row: Int, col: Int): Boolean = {
      (0 until row).forall { i =>
        queens(i) != col && math.abs(queens(i) - col) != math.abs(i - row)
      }
    }

    def solve(queens: Array[Int], row: Int): Option[Array[Int]] = {
      if (row == size) Some(queens.clone())
      else {
        (0 until size).view.flatMap { col =>
          if (isSafe(queens, row, col)) {
            queens(row) = col
            solve(queens, row + 1)
          } else None
        }.headOption
      }
    }
    solve(Array.fill(size)(0), 0)
  }


  def printBoard(queens: Array[Int]): Unit = {
    val size = queens.length
    for {
      row <- 0 until size
    } {
      (0 until size).foreach { col =>
        print(if (queens(row) == col) "1 " else "0 ")
      }
      println()
    }
  }
}
