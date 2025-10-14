package recfun


object Main {
  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("\nBalance tests:")
    println(balance("()".toList))
    println(balance("((()))".toList))
    println(balance("(()".toList))
    println(balance("())(".toList))

    println("\nCountChange tests:")
    println(countChange(7, List(1, 2))) // 4 пути: все 1, одна 2, две 2, три 2

    println("\nN-Queens test (size = 4):")
    nQueens(5) match {
      case Some(sol) =>
        println(sol.mkString("[", ",", "]"))
        printBoard(sol)
      case None =>
        println("No solution")
    }
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if (c < 0 || r < 0 || c > r) 0
    else if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    if (chars.isEmpty) true
    else {
      def helper(cs: List[Char], open: Int): Boolean = {
        if (open < 0) false
        else if (cs.isEmpty) open == 0
        else cs.head match {
          case '(' => helper(cs.tail, open + 1)
          case ')' => helper(cs.tail, open - 1)
          case _   => helper(cs.tail, open)
        }
      }
      helper(chars, 0)
    }
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
    else if (money < 0) 0
    else coins match {
      case Nil => 0
      case head :: tail =>

        countChange(money - head, coins) + countChange(money, tail)
    }
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
    if (size <= 0) return Some(Array.empty[Int])

    def isSafe(col: Int, cols: List[Int]): Boolean = {
      cols.zipWithIndex.forall { case (prevCol, idx) =>
        val rowDistance = idx + 1
        prevCol != col && math.abs(prevCol - col) != rowDistance
      }
    }

    def place(row: Int, cols: List[Int]): Option[List[Int]] = {
      if (row == size) Some(cols.reverse)
      else {
        // try every column in current row
        def tryCols(cs: List[Int]): Option[List[Int]] = cs match {
          case Nil => None
          case c :: rest =>
            if (isSafe(c, cols)) {
              place(row + 1, c :: cols) match {
                case some@Some(_) => some
                case None => tryCols(rest)
              }
            } else tryCols(rest)
        }

        tryCols((0 until size).toList)
      }
    }

    place(0, Nil) match {
      case Some(list) => Some(list.toArray)
      case None => None
    }
  }

  /**
   * Печать доски: 1 — королева, 0 — пустая клетка
   */
  def printBoard(solution: Array[Int]): Unit = {
    val n = solution.length
    for (r <- 0 until n) {
      val row = Array.fill(n)("0")
      row(solution(r)) = "1"
      println(row.mkString(" "))
    }
  }
}