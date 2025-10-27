package recfun

object Main {
  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("Parentheses Balancing")
    val str : List[Char] = "))((abc()".toList
    val res = balance(str)
    println(str)
    println(if (res) " Скобки в порядке" else " Неправильная расстановка скобок")

    println("Counting change")
    val coins : List[Int] = List(2, 3, 1)
    val money : Int = 5
    println(s"money = $money, coins = $coins")
    val countChangeRes = countChange(money, coins)
    println(s"Всего разменов: $countChangeRes")

    println("N-queens problem")
    val size : Int = 8
    println(s"Размер доски: $size x $size")
    nQueens(size) match
      case Some(resBoard) => printBoard(resBoard)
      case None => println("Не удалось решить задачу о N-ферзях")

  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    require(c >= 0 && r >= 0 && c <= r, s"Ошибка: нет коэффициента на этом месте (c=$c, r=$r)")
    if c == 0 || c == r then 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    def loop(chars: List[Char], open: Int): Boolean = {
      if open < 0 then false 
      else chars match
        case Nil => open == 0
        case '(' :: tail => loop(tail, open + 1)
        case ')' :: tail => loop(tail, open - 1)
        case _ :: tail   => loop(tail, open)
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
    if money == 0 then 1
    else if money < 0 || coins.isEmpty then 0
    else
      countChange(money - coins.head, coins) + countChange(money, coins.tail)
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

    def isSafe(queens: Array[Int], row: Int, col: Int): Boolean =
      for i <- 0 until row do
        if queens(i) == col
        then return false
        if (queens(i) - col).abs == (i - row).abs
        then return false
      true

    def solve(row: Int, queens: Array[Int]): Boolean =
      if row == size then true
      else
        for col <- 0 until size do
          if isSafe(queens, row, col) then
            queens(row) = col
            if solve(row + 1, queens) then return true
        false

    val queens = Array.fill(size)(-1)
    if solve(0, queens) then Some(queens) else None
  }

  // Вывод доски с ферзями в консоль 
  def printBoard(board: Array[Int]): Unit = {
    val size = board.length
    for row <- 0 until size do
      val line = (0 until size).map { col =>
        if board(row) == col then "1" else "0"
      }.mkString(" ")
      println(line)
  }



}
