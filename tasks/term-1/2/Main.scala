package recfun
//import common._

object Main {
  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("=" * 30)
    println("Testing pascal")
    println("(The count of rows and columns starts from 0)")
    println("\n")

    println(s"pascal(0, 0) = ${pascal(0, 0)}") // 1
    println(s"pascal(2, 4) = ${pascal(2, 4)}") // 6
    println(s"pascal(3, 5) = ${pascal(3, 6)}") // 20

    println("\n" + "=" * 30)
    println("Testing balance")
    println("\n")

    val testBalance1 = "(This) is a good (line)".toList
    val testBalance2 = "This() is a (bad)) line".toList
    val testBalance3 = ":)".toList

    println(s"Test 1: ${balance(testBalance1)}")
    println(s"Test 2: ${balance(testBalance2)}")
    println(s"Test 3: ${balance(testBalance3)}")

    println("\n" + "=" * 30)
    println("Testing countChange")
    println("\n")

    println(s"countChange(4, List(1,2)) = ${countChange(4, List(1, 2))}") // 3
    println(s"countChange(5, List(2,3)) = ${countChange(5, List(2, 3))}") // 1
    println(s"countChange(0, List(1,2)) = ${countChange(0, List(1, 2))}") // 1

    println("\n" + "=" * 50)
    println("Testing nQueens")
    println("\n")

    val testnQueeens1 = nQueens(2)
    testnQueeens1 match {
      case Some(arr) =>
        println(s"nQueens(2) = ${arr.mkString("[", ",", "]")}")
        println("Board for size 2")
        printBoard(arr)
      case None =>
        println("No solution")
    }

    val testnQueeens2 = nQueens(4)
    testnQueeens2 match {
      case Some(arr) =>
        println(s"nQueens(4) = ${arr.mkString("[", ",", "]")}")
        println("Board for size 4")
        printBoard(arr)
      case None =>
        println("No solution")
    }

    val testnQueeens3 = nQueens(8)
    testnQueeens3 match {
      case Some(arr) =>
        println(s"nQueens(8) = ${arr.mkString("[", ",", "]")}")
        println("Board for size 8")
        printBoard(arr)
      case None =>
        println("No solution")
    }

  }

  /** Exercise 1
    */
  def pascal(c: Int, r: Int): Int = {
    if (c < 0 || c > r) 0
    else if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /** Exercise 2 Parentheses Balancing
    */
  def balance(chars: List[Char]): Boolean = {
    def checkBalance(chars: List[Char], openCount: Int): Boolean = {
      if (openCount < 0) false
      else if (chars.isEmpty) openCount == 0
      else
        chars.head match {
          case '(' => checkBalance(chars.tail, openCount + 1)
          case ')' => checkBalance(chars.tail, openCount - 1)
          case _   => checkBalance(chars.tail, openCount)
        }
    }

    checkBalance(chars, 0)
  }

  /** Exercise 3 Counting Change Write a recursive function that counts how many
    * different ways you can make change for an amount, given a list of coin
    * denominations. For example, there is 1 way to give change for 5 if you
    * have coins with denomiation 2 and 3: 2+3.
    */
  def countChange(money: Int, coins: List[Int]): Int = {
    def count(m: Int, coinList: List[Int]): Int = {
      if (m == 0) 1
      else if (m < 0 || coinList.isEmpty) 0
      else count(m - coinList.head, coinList) + count(m, coinList.tail)
    }

    count(money, coins.sorted)
  }

  /** Excerice 4 N-Queens Problem Write a function that provides a solution for
    * n-queens problem if possible Input parameter represents board size and
    * number of queens Output is an array: index represents row number and value
    * represents column number. Example: nQueens(4): [1,3,0,2] or 0 1 0 0 0 0 0
    * 1 1 0 0 0 0 0 1 0
    */

  def nQueens(size: Int): Option[Array[Int]] = {
    if (size <= 0) return None
    if (size == 2 || size == 3) return None
    if (size == 1) return Some(Array(0))

    def solve(rows: List[Int]): Option[List[Int]] = {
      val currentRow = rows.length

      if (currentRow == size) Some(rows.reverse)
      else {
        def isValidPlacement(col: Int): Boolean = {
          rows.zipWithIndex.forall { case (existingCol, row) =>
            existingCol != col && math.abs(
              existingCol - col
            ) != (currentRow - row)
          }
        }

        (0 until size).collectFirst {
          case col if isValidPlacement(col) =>
            solve(col :: rows)
        }.flatten
      }
    }

    solve(List.empty[Int]).map(_.toArray)
  }

  def printBoard(placement: Array[Int]): Unit = {
    val size = placement.length
    for (row <- 0 until size) {
      for (col <- 0 until size) {
        if (placement(row) == col) print("1 ")
        else print("0 ")
      }
      println()
    }
  }
}
