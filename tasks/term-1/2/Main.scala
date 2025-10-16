package recfun
import common._

import scala.annotation.tailrec

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("Parentheses balancing")
    println(balance("".toList))                               // true
    println(balance("()".toList))                             // true
    println(balance(")()()()".toList))                        // false
    println(balance("(fdf)f9!*&d(fdf(fd(D()f))fsf)".toList))  // true
    println(balance("(fdf)f9!*&d(fdf(fd(D()f))fsf".toList))   // false
    println(balance("(()".toList))                            // false
    println()

    println("Counting change")
    println(countChange(10, List()))          // 0 - пустой список
    println(countChange(5, List(3)))          // 0 - нельзя разменять 5 3ками
    println(countChange(-2, List(1,2,3,4,5))) // 0 - отрицательная сумма
    println(countChange(7, List(1,2,5)))      // 6 - 1 1 1 1 1 1 1
//                                                   1 1 1 1 1 2
//                                                   1 1 1 2 2
//                                                   1 2 2 2
//                                                   1 1 5
//                                                   2 5
    println(countChange(5, List(5)))          // 1 - 5
    println()

    println("N-Queens Problem")
    for (n <- 1 to 10) {
      println(n + " queens:")
      nQueens(n) match {
        case Some(result) =>
          println("Array: " + result.mkString("[", ", ", "]"))
          printBoard(result)
        case None => println("No solution found")
      }
    }

  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || c == r) 1
    else if (c < 0 || c > r) 0
    else pascal(c-1, r-1) + pascal(c, r-1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    @tailrec
    def helper(chars: List[Char], counter: Int): Boolean = {
      if (counter < 0) false
      chars match {
        case Nil => (counter == 0)
        case '(' :: xs => helper(xs, counter+1)
        case ')' :: xs => helper(xs, counter-1)
        case _   :: xs => helper(xs, counter)
      }
    }
    helper(chars, 0)
  }

  /**
   * Exercise 3 Counting Change
   * Write a recursive function that counts how many different ways you can make
   * change for an amount, given a list of coin denominations. For example,
   * there is 1 way to give change for 5 if you have coins with denomiation
   * 2 and 3: 2+3.
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money < 0 || coins.isEmpty) 0
    else if (money == 0) 1
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
    def isPlacementSafe(currentQueens: Array[Int], row: Int, column: Int): Boolean = {
      for (i <- 0 until row) {
        if (currentQueens(i) == column || math.abs(currentQueens(i) - column) == math.abs(i - row)) return false
      }
      true
    }

    def placeQueens(currentQueens: Array[Int], row: Int): Boolean = {
      if (row == size) true
      else {
        for (column <- 0 until size){
          if (isPlacementSafe(currentQueens, row, column)){
            currentQueens(row) = column

            if (placeQueens(currentQueens, row+1)) return true
          }
        }
        false
      }
    }


    var queens = new Array[Int](size)
    if (placeQueens(queens, 0)) Some(queens) else None
  }


  def printBoard(board: Array[Int]): Unit = {
    val size = board.length
    for (row <- 0 until size){
      for (column <- 0 until size){
        if (board(row) == column) print("1 ")
        else print("0 ")
      }
      println()
    }
  }

}