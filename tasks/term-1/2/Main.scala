package recfun

object Main {
  def main(args: Array[String]): Unit = {
    println("Scala Laboratory Work")
    
    println("\n1. Pascal's Triangle:")
    for (row <- 0 to 8) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
    
    println("\n2. Parentheses Balance Check:")
    val test1 = "(example (with nested) parentheses)".toList
    val test2 = "incorrect ) bracket ( placement".toList
    println(s"Test 1: ${balance(test1)}") // true
    println(s"Test 2: ${balance(test2)}") // false
    
    println("\n3. Number of Change Ways:")
    println(s"Change for 10 with coins [1,2,5]: ${countChange(10, List(1,2,5))}")
    println(s"Change for 6 with coins [2,3]: ${countChange(6, List(2,3))}")
    
    println("\n4. N-Queens Problem:")
    val queensSolution = nQueens(5)
    queensSolution match {
      case Some(solution) => 
        println("Solution found:")
        printBoard(solution)
      case None => println("No solution found")
    }
  }

  /**
   *  Pascal's Triangle
   */
  def pascal(column: Int, row: Int): Int = {
    if (column < 0 || column > row) 0
    else if (column == 0 || column == row) 1
    else pascal(column - 1, row - 1) + pascal(column, row - 1)
  }

  /**
   * Parentheses Balance
   */
  def balance(chars: List[Char]): Boolean = {
    def checkBalance(chars: List[Char], openCount: Int): Boolean = {
      if (chars.isEmpty) openCount == 0
      else if (openCount < 0) false
      else chars.head match {
        case '(' => checkBalance(chars.tail, openCount + 1)
        case ')' => checkBalance(chars.tail, openCount - 1)
        case _ => checkBalance(chars.tail, openCount)
      }
    }
    checkBalance(chars, 0)
  }

  /**
   * Counting Change
   */
  def countChange(amount: Int, coins: List[Int]): Int = {
    if (amount == 0) 1
    else if (amount < 0 || coins.isEmpty) 0
    else {
      // Use first coin + skip first coin
      countChange(amount - coins.head, coins) + countChange(amount, coins.tail)
    }
  }

  /**
   *  N-Queens Problem
   */
  def nQueens(boardSize: Int): Option[Array[Int]] = {
    
    def canPlaceQueen(positions: Array[Int], currentRow: Int, testCol: Int): Boolean = {

