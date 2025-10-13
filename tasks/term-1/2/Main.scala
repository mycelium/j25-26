package recfun

object Main {
  def main(args: Array[String]): Unit = {
    println("    Task 1. Pascal function")
    println("1. 10 rows of Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
    println{"2. Another example, 5th row; 3rd column (must be 6):"}
    print(pascal(2, 4))
    println()
    println("    Task 2. Balance function")
    println("Balance test:")
    println("1. test for: '(i (love) (parallel programming))'")
    println(balance("(i (love) (parallel programming))".toList))
    println("2. test for: '(i (hate (parallel programming))'")
    println(balance("(i (hate (parallel programming))".toList))

    println("\n    Task 3. Change count function:")
    println("Count change test:")
    println("1. testing case: giving change for 5 with coins with denomination 2 and 3:")
    println(countChange(5, List(2,3)))
    println("1. testing case: giving change for 5 with coins with denomination 1, 2, 3, 4:")
    println(countChange(5, List(1, 2, 3, 4)))

    println("\n    Task 4. N-Queens function:")
    println("1. 4-Queens test:")
    val queens = nQueens(4)
    queens match {
      case Some(solution) => 
        println("Array: " + solution.mkString("[", ", ", "]"))
        printBoard(solution)
      case None => println("No solution found")
    }
    println("\n2. 6-Queens test:")
    val queens2 = nQueens(6)
    queens2 match {
      case Some(solution) => 
        println("Array: " + solution.mkString("[", ", ", "]"))
        printBoard(solution)
      case None => println("No solution found")
    }
  }


  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }


  def balance(chars: List[Char]): Boolean = {
    def balanceRecursiveHelper(chars: List[Char], count: Int): Boolean = {
      if (chars.isEmpty) count == 0
      else if (count < 0) false
      else chars.head match {
        case '(' => balanceRecursiveHelper(chars.tail, count + 1)
        case ')' => balanceRecursiveHelper(chars.tail, count - 1)
        case _ => balanceRecursiveHelper(chars.tail, count)
      }
    }
    balanceRecursiveHelper(chars, 0)
  }

  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) 1
    else if (money < 0 || coins.isEmpty) 0
    else countChange(money - coins.head, coins) + countChange(money, coins.tail)
  }
  
  def nQueens(size: Int): Option[Array[Int]] = {
    def safeToPlace(queens: Array[Int], row: Int, col: Int): Boolean = {
      for (i <- 0 until row) {
        if (queens(i) == col || 
            queens(i) - i == col - row || 
            queens(i) + i == col + row) {
          return false
        }
      }
      true
    }

    def isSolved(queens: Array[Int], row: Int): Boolean = {
      if (row == size) true
      else {
        for (col <- 0 until size) {
          if (safeToPlace(queens, row, col)) {
            queens(row) = col
            if (isSolved(queens, row + 1)) return true
          }
        }
        false
      }
    }

    val queens = Array.fill(size)(-1)
    if (isSolved(queens, 0)) Some(queens)
    else None
  }

  def printBoard(solution: Array[Int]): Unit = {
    val size = solution.length
    for (i <- 0 until size) {
      for (j <- 0 until size) {
        if (solution(i) == j) print("1 ") 
        else print("0 ")
      }
      println()
    }
  }
}
