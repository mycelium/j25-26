//package recfun
//import common._

object Main {
  def main(args: Array[String]): Unit =  {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("\nBalance")
    println(balance("(balance (test) scala())".toList)) // true
    println(balance("()(()()".toList)) // false

    println("\nCount change")
    println(countChange(5, List(2,3)))  //1
    println(countChange(5, List(2,1,3))) //5

    println("\nQueens")
    nQueens(4) match {
      case Some(solution) => printBoard(solution)
      case None => println("No solution found")
    }

    println("\nQueens")
    nQueens(6) match {
      case Some(solution) => printBoard(solution)
      case None => println("No solution found")
    }
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
      if (c==0 || c==r) 1
      else if(c<0 || c>r)0
      else pascal(c-1,r-1)+pascal(c,r-1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
   def balanceHelper (chars: List[Char],count: Int): Boolean ={
     if (count < 0) false
     else chars match{
      case Nil => count ==0
      case '(':: tail => balanceHelper(tail,count+1)
      case ')':: tail => balanceHelper(tail,count-1)
      case _ :: tail => balanceHelper(tail,count)
     }
   }
   balanceHelper(chars,0);
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
    else if (money <0 || coins.isEmpty) 0
    else{
      countChange(money-coins.head, coins)+countChange(money,coins.tail)
    }
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
  def isSafe(queens: List[Int], col: Int): Boolean = {
    val row = queens.length
    queens.zipWithIndex.forall { case (qCol, qRow) =>
      qCol != col && math.abs(qCol - col) != math.abs(qRow - row)
    }
  }

  def solve(queens: List[Int]): Option[List[Int]] = {
    if (queens.length == size) Some(queens)
    else {
      var result: Option[List[Int]] = None
      var col = 0
      while (col < size && result.isEmpty) {
        if (isSafe(queens, col)) {
          result = solve(queens :+ col)
        }
        col += 1
      }
      result
    }
  }

  solve(List.empty[Int]).map(_.toArray)
}


def printBoard(solution: Array[Int]): Unit = {
       println(s"Solution: [${solution.mkString(", ")}]")
       val size = solution.length
       for (row <- 0 until size) {
          for (col <- 0 until size) {
          if (solution(row) == col) print("1 ") else print("0 ")
        }
       println()
    }
    
  }

}