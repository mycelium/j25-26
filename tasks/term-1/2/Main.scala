package recfun
//import common._

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
    println(pascal(3,5))

    println("'((((1234+5+4)+5)+4)'", balance("((((1234+5+4)+5)+4)".toList))
    println("'((((1234+5+4)+5)+4)+5)'", balance("((((1234+5+4)+5)+4)+5)".toList))

    println(countChange(5, List(2,3,1)))

    printBoard(nQueens(4))
    printBoard(nQueens(1))
    printBoard(nQueens(2))
    printBoard(nQueens(7))
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if (c==0 || c == r) 1
    else pascal(c-1, r-1) + pascal(c, r-1)
  }


  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    def brackets(chars: List[Char], count: Int): Boolean = {
      if (count < 0) false
      else if (chars.isEmpty) count == 0
      else chars.head match {
        case '(' => brackets(chars.tail, count + 1)
        case ')' => brackets(chars.tail, count - 1)
        case _ => brackets(chars.tail, count)
      }
    }
    brackets(chars, 0)
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
    else countChange(money, coins.tail) + countChange(money - coins.head, coins)
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
    def canPlace(queens: Array[Int], row: Int, col: Int): Boolean = {
      (0 until row).forall { r =>
        val c = queens(r)
        c != col && math.abs(c - col) != row - r
      }
    }

    def placement(row: Int, current: Array[Int]): Option[Array[Int]] = {
      if (row == size) Some(current.clone())
      else {
        @annotation.tailrec
        def tryColumns(col: Int): Option[Array[Int]] = {
          if (col >= size) None
          else if (canPlace(current, row, col)) {
            current(row) = col
            placement(row + 1, current) match {
              case Some(solution) => Some(solution)
              case None => tryColumns(col + 1)
            }
          } else {
            tryColumns(col + 1)
          }
        }
        tryColumns(0)
      }
    }

    placement(0, Array.ofDim[Int](size))
  }

  def printBoard (solution: Option[Array[Int]]): Unit = {
    solution match {
      case Some(board) =>
        val size = board.length
        println(s"Решение: [${board.mkString(", ")}]")

        for (row <- 0 until size) {
          for (col <- 0 until size) {
            if (board(row) == col) print(" 1 ")  // Ферзь
            else print(" 0 ")  // Темная клетка
          }
          println()
        }

      case None =>
        println("Решение не найдено")
    }
  }
}
