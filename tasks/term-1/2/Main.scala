package recfun
import common._

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if(c<=0 || r<=0 || c>r) 0
    else if(c==1 || c==r) 1
    else pascal(c-1, r-1) + pascal(c, r-1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    var parentheesesCounter: Int = 0
    for(c <- chars){
      if(c == '(') parentheesesCounter += 1
      if(c == ')') parentheesesCounter -= 1
      if(parentheesesCounter < 0) false
    }
    if(parentheesesCounter == 0) true
    else false
  }

  /**
   * Exercise 3 Counting Change
   * Write a recursive function that counts how many different ways you can make
   * change for an amount, given a list of coin denominations. For example,
   * there is 1 way to give change for 5 if you have coins with denomiation
   * 2 and 3: 2+3.
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    def removeIdentical(strings: Array[Array[Int]]): Array[Array[Int]] = {
      def compare(a: Array[Int], b: Array[Int]): Int = {
        for(i <- 0 to a.size-1){
          if(a(i) > b(i)) return 1
          else if(a(i) < b(i)) return -1
        }
        return 0
      }
      for(i <- 0 to strings.size-2){
        for(j <- 0 to strings.size-i-2){
          if(compare(strings(j), strings(j+1)) > 0){
            var buf = strings(j)
            strings(j) = strings(j+1)
            strings(j+1) = buf
          }
        }
      }
      var res: Array[Array[Int]] = Array(strings(0))
      for(i <- 1 to strings.size-1){
        if(compare(strings(i-1), strings(i)) != 0){
          res = res :+ strings(i)
        }
      }
      res
    }
    def recursiveChangeCreator(money: Int, coins: List[Int]): Array[Array[Int]] = {
      var resultsList = Array.empty[Array[Int]]
      for(coinIndex <- 0 to coins.size - 1){
        if(money == coins(coinIndex)){
          var result = Array.fill(coins.length)(0)
          result(coinIndex) = 1
          resultsList = resultsList :+ result
        }
        else if (money > coins(coinIndex)){
          var resultsPart = recursiveChangeCreator(money - coins(coinIndex), coins)
          for(result <- resultsPart) result(coinIndex) += 1
          resultsList = resultsList ++ resultsPart
        }
      }
      resultsList
    }
    removeIdentical(recursiveChangeCreator(money, coins)).size
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
    def recursiveQueenPlacing(placed: Array[Int], size: Int, row: Int): Option[Array[Int]] = {
      for( i <- 0 to size - 1){
        var allowed: Boolean = true
        for(j <- 0 to row - 1){
          if(placed(j) == i || placed(j) == i - row + j || placed(j) == i + row - j){
            allowed = false
          }
        }
        if(allowed){
          var newPlaced = placed
          newPlaced(row) = i
          if(row == size - 1) return Some(newPlaced)
          else{
            var res = recursiveQueenPlacing(newPlaced, size, row+1)
            if(res.isDefined) return res
          } 
        }
      }
      return None
    }
    if(size < 1 || size == 2 || size == 3) None
    else{
      recursiveQueenPlacing(Array.ofDim[Int](size), size, 0)
    }
  }

  def nQueensPrintTable(size: Int, queens: Option[Array[Int]]): Unit = {
    if(queens.isDefined){
    	for(i <- 0 to size - 1){
	      for(j <- 0 to size - 1){
	        if(j == queens.get(i)) print("1 ")
	        else print("0 ")
	      }
	      println()
	    }
    }
	  else println("No solution :(")
  }
}
