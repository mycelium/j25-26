
//import common._

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("\nParentheses Balancing ")
    val test1 = "(21−18)+7×(16−(9+5))"
    val test2 = "(They explained the theory (which was quite complex (and took hours to cover))) . (But he wasn’t listening)"
    val test3 = "())("
    println(s"$test1 => ${balance(test1.toList)}")
    println(s"$test2 => ${balance(test2.toList)}")
    println(s"$test3 => ${balance(test3.toList)}")

    println("\n Counting Change ")
    val money = 5
    val coins = List(1, 2, 3)
    val ways = countChange(money, coins)
    println(s"There are $ways ways to make change for $money using coins $coins")


    println("\n N-Queens Problem ")
    val n = 4
    nQueens(n) match {
      case Some(solution) =>
        println(s"One solution for $n-Queens is: ${solution.mkString("[", ", ", "]")}")
        printBoard(solution)
      case None =>
        println(s"No solution found for $n-Queens.")
    }
  }


  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if ( c == 0 || c == r ) 1
    else pascal (c -1 , r - 1) + pascal (c, r - 1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    def loop(chars: List[Char], open: Int): Boolean = {
      if (open < 0) false
      else if (chars.isEmpty) open == 0
      else chars.head match {
        case '(' => loop(chars.tail, open + 1)
        case ')' => loop(chars.tail, open - 1)
        case _   => loop(chars.tail, open)
      }
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
    if (money == 0) 1
    else if (money < 0) 0
    else if (coins.isEmpty) 0
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
     def isSafe(queens: List[Int], col: Int): Boolean = {
       val row = queens.length
       queens.zipWithIndex.forall { case (c, r) =>
         c != col && math.abs(row - r) != math.abs(col - c)
       }
     }

     def addQueens(k: Int): List[List[Int]] = {
       if (k == 0) List(List())
       else
         for {
           queens <- addQueens(k - 1)
           col <- 0 until size
           if isSafe(queens, col)
         } yield queens :+ col
     }

     addQueens(size).headOption.map(_.toArray)
   }

  def printBoard(solution: Array[Int]): Unit = {
    val size = solution.length
    println("\nBoard visualization:")
    for (row <- 0 until size) {
      for (col <- 0 until size) {
        if (solution(row) == col) print(" Q ")
        else print(" . ")
      }
      println()
    }
  }
}