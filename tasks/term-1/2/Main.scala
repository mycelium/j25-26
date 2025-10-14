//package recfun
// import common._

object Main {
  def main(args: Array[String]): Unit = { 
    println ("---Task 1 ---")
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
    println(pascal(3,5))
    println()

    println ("---Task 2---")
    println(balance("(just (hundred) labs)".toList)) // true
    println(balance("())(".toList)) // true
    println(balance("".toList)) // true
    println(balance("(at)t(e)ntion(!* ^))".toList)) // false
    println(balance("(()".toList)) // false
    println()

    println("---Task 3---")
    println(countChange(-1, List(1,2,5))) // 0 - отриц сумма
    println(countChange(5, List())) // 0 - нет монет
    println(countChange(3, List(2))) // 0 (нельзя 3 разменять двойками)
    println(countChange(4, List(1, 2)))  // 3 (1,1,1,1  1,1,2  2,2)
    println()

    println("--Task 4---")
    List(3, 4, 5).foreach { n =>
    println(s"$n-Queens test:")
    nQueens(n) match {
      case Some(result) => 
        println("Array: " + result.mkString("[", ", ", "]"))
        printBoard(result)
      case None => println("No solution found")
      }
    println()
    }
  }

  /**
   * Exercise 1 
   */
  def pascal(c: Int, r: Int): Int = {
    if (c<0 || c>r) 0
    else if (c==0 || c==r) 1
    else pascal(c-1, r-1) + pascal(c, r-1)
  }

  /**
   * Exercise 2 Parentheses Balancing 
   */
  def balance(chars: List[Char]): Boolean = {
    def inside(chars: List[Char], count: Int = 0) : Boolean = {
      chars match {
        case Nil => count == 0
        case '(' :: tail => inside(tail, count+1)
        case ')' :: tail => inside(tail, count-1)
        case _ :: tail => inside(tail, count)
      }
    }
    inside(chars)
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
   * функция, принимающая размер шахматной доски и возвращающая решение по расстановке такого же количества королев на этом поле таким образом, чтобы они не били друг друга. 
   * Результат работы функции - массив номеров колонок, где размещена королева, индекс является номером строки. Отдельно реализовать печать доски, где 1 - королева, 0 - пустая клетка (N-Queen problem)
   */

  def nQueens(n: Int): Option[Array[Int]] = {
    def isCorrect(queens: Array[Int], row: Int, col: Int): Boolean = {
      (0 until row).forall { r => //(r <- 0 until n)
        val c = queens(r)
        c != col && //та же колонка
        (math.abs(c-col) != math.abs(r-row)) //диагональ
        }
    }
    def placeQueens(row: Int, queens: Array[Int]) : Boolean = {
      if (row == n) true
      else {
        (0 until n).exists {col =>
          isCorrect(queens, row, col) && {
            queens(row) = col
            placeQueens(row+1, queens)
          }
        }
      }
    }
    val queens = new Array[Int](n)
    if (placeQueens(0, queens)) Some(queens) else None
  }
  def printBoard(result: Array[Int]): Unit = {
    val n = result.length
    for (r <- 0 until n){
      for (c <- 0 until n){
        if (result(r) == c) print("1 ")
        else print("0 ")
      }
      println()
    }
  }
}
