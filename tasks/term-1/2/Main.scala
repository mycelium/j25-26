package recfun
// import common._

object Main {
  def main(args: Array[String]) = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("\n=== Balance tests ===")
    println(balance("((1+2)*3)".toList))
    println(balance("((1+2)*3".toList))
    println(balance("(1+2)*3)(".toList))     

    println("\n=== Count Change ===")
    println(countChange(5, List(2, 3))) 
    println(countChange(4, List(1, 2)))

    println("\n=== N Queens ===")
    nQueens(4) match {
      case Some(sol) =>
        println("Solution: " + sol.mkString("[", ", ", "]"))
        printBoard(sol)
      case None =>
        println("No solution found.")
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
    def check(rest: List[Char], opened: Int): Boolean = {
      if (opened < 0) false
      else rest match {
        case Nil          => opened == 0
        case '(' :: tail  => check(tail, opened + 1)
        case ')' :: tail  => check(tail, opened - 1)
        case _   :: tail  => check(tail, opened)
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
    def count(amount: Int, remaining: List[Int]): Int = {
      if (amount == 0) 1
      else if (amount < 0 || remaining.isEmpty) 0
      else count(amount, remaining.tail) + count(amount - remaining.head, remaining)
    }
    count(money, coins.sorted)
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

    if (size == 2 || size == 3)
      return None

    val board = Array.fill(size)(-1)

    // Проверка на врозможность поставить
    def isSafe(row: Int, col: Int): Boolean = {
      for (r <- 0 until row) {
        val c = board(r)
        val sameColumn = c == col
        val sameDiag = math.abs(c - col) == math.abs(r - row)
        if (sameColumn || sameDiag)
          return false
      }
      true
    }

    def place(row: Int): Boolean = {
      if (row == size)
        true // все расставлены
      else {
        for (col <- 0 until size) {
          if (isSafe(row, col)) {
            board(row) = col
            if (place(row + 1))
              return true
            board(row) = -1
          }
        }
        false
      }
    }

    if (place(0)) Some(board.clone()) else None
  }

  def printBoard(solution: Array[Int]): Unit = {
    val n = solution.length
    println()
    for (r <- 0 until n) {
      for (c <- 0 until n) {
        if (solution(r) == c)
          print("1 ")
        else
          print("0 ")
      }
      println()
    }
    println()
  }
}
