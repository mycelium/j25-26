package recfun
import common._

object Main {
  def main(args: Array[String]) = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
    println("Проверка скобок ((a b)c)((c(h(d a) m))e): " + balance("((a b)c)((c(h(d a) m))e)".toList))
    println("(c(a b))((d): " + balance("(c(a b))((d)".toList))
    println("Количество возможных вариантов размена суммы: " + countChange(5, List(2, 3)))
    println("Размещение ферзей:")
    nQueens(4) match {
      case Some(solution) => printBoard(solution)
      case None =>
    }
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int) :Int =
  {
    if (c < 0 || c > r) return 0
    else if (r == 0 || c == 0 || c == r ) return 1
    else return pascal(c-1, r-1) + pascal(c-1, r-1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean =
  {
    def counterFunc(chars: List[Char], counter: Int): Boolean = {
      if (chars.isEmpty) return counter == 0
      else if (counter < 0) return false
      else return {
        chars.head match {
          case '(' => counterFunc(chars.tail, counter + 1)
          case ')' => counterFunc(chars.tail, counter - 1)
          case _ => counterFunc(chars.tail, counter)
        }
      }
    }
    counterFunc(chars, 0)
  }

  /**
   * Exercise 3 Counting Change
   * Write a recursive function that counts how many different ways you can make
   * change for an amount, given a list of coin denominations. For example,
   * there is 1 way to give change for 5 if you have coins with denomiation
   * 2 and 3: 2+3.
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) return 1
    else if (coins.isEmpty || money < 0) return 0
    else return countChange(money - coins.head, coins) + countChange(money, coins.tail)
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
  def nQueens(size: Int): Option[Array[Int]] =
  {
    def isSafe(board: Array[Int], row: Int, col: Int): Boolean =
    {
      for (i <- 0 until row)
      {
        if (board(i) == col) return false
        if (math.abs(board(i) - col) == row - i) return false
      }
      return true
    }
    def findSolution(board : Array[Int], row: Int): Option[Array[Int]] = {
      if (row == size) return Some(board.clone())
      for (col <- 0 until size)
      {
        if (isSafe(board, row, col))
        {
          board(row) = col
          findSolution(board, row + 1) match
          {
            case Some(solution) => return Some(solution)
            case None =>
          }
        }
      }
      None
    }
    findSolution(Array.fill(size)(-1), 0)
  }

  def printBoard(board: Array[Int]): Unit = {
    val size = board.length
    for (i <- 0 until size) {
      for (j <- 0 until size) {
        if (board(i) == j) print("1 ") else print("0 ")
      }
      println()
    }
  }

}