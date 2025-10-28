object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("\n=== Проверка баланса скобок ===")
    val testCases = List(
      "(just (an) example)",
      "())(",
      "((()))",
      "()()()",
      "((())",
      "()))(",
      "()()))(("
    )
    testCases.foreach { str =>
      println(s"'$str' - баланс: ${balance(str.toList)}")
    }

    println("\n=== Количество вариантов размена ===")
    val moneyTestCases = List(
      (4, List(1, 2)),
      (10, List(1, 2, 5)),
      (5, List(1, 2, 3)),
      (0, List(1, 2)),
      (3, List(2))
    )
    moneyTestCases.foreach { case (money, coins) =>
      println(s"Размен $money монетами [${coins.mkString(", ")}]: ${countChange(money, coins)} вариантов")
    }

    println("\n=== Задача о N ферзях ===")
    for (size <- 1 to 9) {
      println(s"\nРешение для доски $size x $size:")
      nQueens(size) match {
        case Some(solution) =>
          println(s"Расположение ферзей: ${solution.mkString("Array(", ", ", ")")}")
          if (size <= 6) {
            println("Доска:")
            printBoard(solution)
          }
        case None =>
          println("Решение не найдено")
      }
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
    def check(chars: List[Char], count: Int): Boolean = {
      if (count < 0) false
      else chars match {
        case Nil => count == 0
        case '(' :: tail => check(tail, count + 1)
        case ')' :: tail => check(tail, count - 1)
        case _ :: tail => check(tail, count)
      }
    }
    check(chars, 0)
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
    def isSafe(board: Array[Int], row: Int, col: Int): Boolean = {
      (0 until row).forall { i =>
        board(i) != col &&
          math.abs(board(i) - col) != row - i
      }
    }

    def solve(board: Array[Int], row: Int): Boolean = {
      if (row >= size) true
      else {
        (0 until size).exists { col =>
          if (isSafe(board, row, col)) {
            board(row) = col
            if (solve(board, row + 1)) true
            else {
              board(row) = -1
              false
            }
          } else false
        }
      }
    }

    val board = Array.fill(size)(-1)
    if (solve(board, 0)) Some(board)
    else None
  }


  def printBoard(solution: Array[Int]): Unit = {
    val n = solution.length
    for (row <- solution) {
      for (col <- 0 until n) {
        if (col == row) print("1 ")
        else print("0 ")
      }
      println()
    }
  }

}
