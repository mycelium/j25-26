object Main {
  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
    
    println()
    println("Parentheses Balancing")
    println("()")           
    println(balance("()".toList))  
    println("(())")         
    println(balance("(())".toList))    
    println("(if (x > 10) x else 0)") 
    println(balance("(if (x > 0) x else 0)".toList))
    println("(2-1)+((10-4)*(5+3))")
    println(balance("(2-1)+((10-4)*(5+3))".toList))
    println("(2-1(+((10-4)*(5+3))")
    println(balance("(2-1(+((10-4)*(5+3))".toList))
    println("())")   
    println(balance("())".toList))  
    println(")()")    
    println(balance(")()".toList))    

    println()
    println("Counting Change")
    println(countChange(4, List(1, 2)))        
    println(countChange(5, List(1, 2, 5)))     
    println(countChange(10, List(2, 5, 3, 6)))

    println()
    println("N-Queens Problem")
    List(3, 4, 5, 6, 7).foreach { n =>
      nQueens(n) match {
        case Some(result) => 
          println("Array: " + result.mkString("[", ", ", "]"))
          printBoard(result)
        case None => println("No solution found")
      }
      println()
    }
  }

  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  def balance(chars: List[Char]): Boolean = {
    def pm(chars: List[Char], count: Int): Boolean = chars match {
      case Nil => count == 0                         
      case '(' :: tail => pm(tail, count + 1)     
      case ')' :: tail =>                             
        if (count <= 0) false 
        else pm(tail, count - 1)
      case _ :: tail => pm(tail, count)            
    }
    pm(chars, 0)
  }

  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) 1
    else if(money < 0 || coins.isEmpty) 0
    else {
      countChange(money - coins.head, coins) + countChange(money, coins.tail) 
    }
  }
  
  def nQueens(size: Int): Option[Array[Int]] = {
    val chessBoard = Array.fill(size)(-1)
  
    def isPositionSafe(currentRow: Int, currentCol: Int): Boolean = {
      for (previousRow <- 0 until currentRow) {
        val queenCol = chessBoard(previousRow)
        val sameColumn = queenCol == currentCol
        val sameDiagonal = math.abs(currentRow - previousRow) == math.abs(currentCol - queenCol)
        if (sameColumn || sameDiagonal) return false
      }
      true
    }
  
    def placeQueensS(startRow: Int): Boolean = {
      if (startRow == size) return true
  
      for (column <- 0 until size) {
        if (isPositionSafe(startRow, column)) {
          chessBoard(startRow) = column
          if (placeQueensS(startRow + 1)) return true
          chessBoard(startRow) = -1
        }
      }
      false
    }
  
    if (placeQueensS(0)) Some(chessBoard.clone()) else None
  }

  def printBoard(board: Array[Int]): Unit = {
    for (row <- board.indices) {
      for (col <- board.indices) {
        if (board(row) == col) print("1 ") else print("0 ")
      }
      println()
    }
  }
}