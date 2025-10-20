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
    println(balance("()()".toList))   // true
    println(balance("(())".toList))   // true
    println(balance("(()".toList))    // false
    println(balance("())(".toList))   // false

    println("\nCount Change:")
    println(countChange(5, List(1, 2, 3))) // 5 способов

    println("\nN-Queens:")
    val n = 8
    nQueens(n) match {
      case Some(solution) =>
        println("Solution: " + solution.mkString("[", ", ", "]"))
        printBoard(solution)
      case None => println("No solution found.")
    }
  }

  /** Exercise 1 — Pascal's Triangle */
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /** Exercise 2 — Balance parentheses */
  def balance(chars: List[Char]): Boolean = {
    def loop(xs: List[Char], count: Int): Boolean = {
      if (count < 0) false
      else if (xs.isEmpty) count == 0
      else xs.head match {
        case '(' => loop(xs.tail, count + 1)
        case ')' => loop(xs.tail, count - 1)
        case _   => loop(xs.tail, count)
      }
    }
    loop(chars, 0)
  }

  /** Exercise 3 — Counting Change */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) 1
    else if (money < 0 || coins.isEmpty) 0
    else
      countChange(money, coins.tail) + countChange(money - coins.head, coins)
  }

  /** Exercise 4 — N Queens Problem */
  def nQueens(size: Int): Option[Array[Int]] = {

    def isSafe(queens: List[Int], col: Int): Boolean = {
      val row = queens.length
      queens.zipWithIndex.forall { case (c, r) =>
        c != col && math.abs(c - col) != math.abs(r - row)
      }
    }

    def placeQueens(row: Int, queens: List[Int]): List[List[Int]] = {
      if (row == size) List(queens)
      else {
        (0 until size).toList.flatMap { col =>
          if (isSafe(queens, col))
            placeQueens(row + 1, queens :+ col)
          else
            Nil
        }
      }
    }

    val solutions = placeQueens(0, Nil)
    solutions.headOption.map(_.toArray)
  }

  def printBoard(solution: Array[Int]): Unit = {
    val n = solution.length
    for (r <- 0 until n) {
      for (c <- 0 until n) {
        if (solution(r) == c) print("1 ") else print("0 ")
      }
      println()
    }
  }
}
