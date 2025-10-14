package recfun
object Main {
  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

     //тестирование
    println("balance:")
    println("Cat in box: " + balance("(cat in box)".toList)) // true
    println("Cat with tail: " + balance("(cat (with tail))".toList)) // true
    println("Cat without sleep: " + balance("(cat not sleeping))".toList)) // false
    println("Fighting cats: " + balance("())(".toList)) // false

    println("countChange:")
    println("4 coins for [1,2]: " + countChange(4, List(1,2))) // 3
    println("5 coins for [2,3]: " + countChange(5, List(2,3))) // 1  
    println("10 coins for [1,2,5]: " + countChange(10, List(1,2,5))) // 10

    println("\nTesting nQueens:")
    nQueens(4) match {
      case Some(solution) => printBoard(solution)
      case None => println("No solution for n=4")
    }
  }

  /**
   * Exercise 1 - Pascal's Triangle
   */
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    def balanceHelper(chars: List[Char], count: Int): Boolean = {
      if (chars.isEmpty) count == 0
      else if (count < 0) false
      else chars.head match {
        case '(' => balanceHelper(chars.tail, count + 1)
        case ')' => balanceHelper(chars.tail, count - 1)
        case _ => balanceHelper(chars.tail, count)
      }
    }
    
    balanceHelper(chars, 0)
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
   * Exercise 4 N-Queens Problem
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
      (0 until row).forall { i => 
        queens(i) != col && 
        (queens(i) - i) != (col - row) && 
        (queens(i) + i) != (col + row)
      }
    }

    //возврат Boolean в solve:
    def solve(queens: Array[Int], row: Int): Boolean = {
      if (row == size) true
      else {
        (0 until size).exists { col =>
          if (isSafe(queens, row, col)) {
            queens(row) = col
            if (solve(queens, row + 1)) true
            else {
              queens(row) = -1
              false
            }
          } else false
        }
      }
    }

    val queens = Array.fill(size)(-1)
    if (solve(queens, 0)) Some(queens) else None
  }

  //для печати шахматной доски
  def printBoard(queens: Array[Int]): Unit = {
    val size = queens.length
    for (row <- 0 until size) {
      for (col <- 0 until size) {
        if (queens(row) == col) print("1 ") else print("0 ")
      }
      println()
    }
    println(s"Solution: ${queens.mkString("[", ", ", "]")}")
  }
}