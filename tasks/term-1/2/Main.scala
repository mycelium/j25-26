  package recfun


  object Main {
    def main(args: Array[String]) {
      println("Pascal's Triangle")
      for (row <- 0 to 10) {
        for (col <- 0 to row)
          print(pascal(col, row) + " ")
        println()
      }

      // тесты
      println("\nТест скобок:")
      println("(())()(((()))) " + balance("(())()(((())))".toList)) // true
      println("()()(())((()) " + balance("()()(())((())".toList)) // false

      println("\nТест размена:")
      println(countChange(4, List(1,2))) // 3
      val n = 5
      println("\nРешение задачи королев для " + n +" королев")
      nQueens(n) match {
        case Some(solution) => printBoard(solution)
        case None => println("No solution found")
      }
    }

    /**
     * Exercise 1
     */
    def pascal(c: Int, r: Int): Int = {
      if (c == 0 || c == r) 1
      else if (c < 0 || c > r) 0
      else pascal(c - 1, c - 1) + pascal(c, r - 1)
    }

    /**
     * Exercise 2 Parentheses Balancing
     */
    def balance(chars: List[Char]): Boolean = {
      def checkBalance(chars: List[Char], count: Int): Boolean = {
        if (count < 0) false
        else chars match {
          case Nil => count == 0
          case '(' :: tail => checkBalance(tail, count + 1)
          case ')' :: tail => checkBalance(tail, count - 1)
          case _ :: tail => checkBalance(tail, count)
        }
      }
      checkBalance(chars, 0)
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
      else if (money < 0 || coins.isEmpty) 0
      else countChange(money, coins.tail) + countChange(money - coins.head, coins)
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

    def nQueens(n: Int): Option[Array[Int]] = {
      def provQueen(queens: Array[Int], row: Int, col: Int): Boolean = {
        (0 until row).forall { r =>
          val c = queens(r)
          c != col &&
            (math.abs(c-col) != math.abs(r-row))
        }
      }
      def newQueens(row: Int, queens: Array[Int]) : Boolean = {
        if (row == n) true
        else {
          (0 until n).exists {col =>
            provQueen(queens, row, col) && {
              queens(row) = col
              newQueens(row+1, queens)
            }
          }
        }
      }
      val queens = new Array[Int](n)
      if (newQueens(0, queens)) Some(queens) else None
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
