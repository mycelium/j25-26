package recfun

object Main {
  def main(args: Array[String]) = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
    println("Тестирование Exercise 2 Parentheses Balancing:")
    println("(2+5) * ((4 * 10)) : " + balance("(2+5) * ((4 * 10))".toList))
    println("2+5) * (4 * 10)) : " + balance("2+5) * (4 * 10))".toList))
    println("Тестирование Exercise 3 Counting Change:")
    println("2, List(1,2): " + countChange(2, List(1, 2)))
    println("7, List(1,2,3): " + countChange(7, List(1, 2, 3)))
    println("Тестирование Excerice 4 N-Queens Problem:")
    nQueens(5) match {
      case Some(finding) => printBoard(finding)
      case None =>
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
    def auxiliaryDef(chars: List[Char], count: Int): Boolean = {
      if (chars.isEmpty) {
        count == 0
      }
      else if (count < 0) {
        false
      }
      else chars.head match {
        case '(' => auxiliaryDef(chars.tail, count + 1)
        case ')' => auxiliaryDef(chars.tail, count - 1)
        case _ => auxiliaryDef(chars.tail, count)

      }
    }

    auxiliaryDef(chars, 0)
  }

  /**
   * Exercise 3 Counting Change
   * Write a recursive function that counts how many different ways you can make
   * change for an amount, given a list of coin denominations. For example,
   * there is 1 way to give change for 5 if you have coins with denomiation
   * 2 and 3: 2+3.
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) {
      1
    }
    else if (money < 0 || coins.isEmpty) {
      0
    }
    else {
      countChange(money - coins.head, coins) + countChange(money, coins.tail)
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
    def notEaten(positionQ: Array[Int], rowNewQ: Int, colNewQ: Int): Boolean = {
      for (i <- 0 until rowNewQ) {
        if (positionQ(i) == colNewQ || math.abs(positionQ(i) - colNewQ) == math.abs(i - rowNewQ)) {
          return false
        }

      }
      true
    }

    def finding(row: Int, positionQ: Array[Int]): Boolean = {
      if (row == size) {
        true
      }
      else {
        (0 until size).exists { y =>
          notEaten(positionQ, row, y) && {
            positionQ(row) = y
            finding(row + 1, positionQ)
          }
        }
      }
    }
    val positionQ = new Array[Int](size)
    val findVal = finding(0, positionQ)
    if (findVal) Some(positionQ) else None

  }

  def printBoard(positionQ: Array[Int]): Unit = {
    val size = positionQ.length

    for(x <- 0 until size)
    {
      for (y <- 0 until size)
      {
        if (positionQ(x) == y) print("1 ") else print("0 ")
      }
      println()
    }
  }

}
