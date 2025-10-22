error id: file:///C:/Users/Putyata/Desktop/Study/Sem%205/Java/j25-26/tasks/term-1/2/Main.scala:scala/Int#
file:///C:/Users/Putyata/Desktop/Study/Sem%205/Java/j25-26/tasks/term-1/2/Main.scala
empty definition using pc, found symbol in pc: scala/Int#
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -Int#
	 -scala/Predef.Int#
offset: 318
uri: file:///C:/Users/Putyata/Desktop/Study/Sem%205/Java/j25-26/tasks/term-1/2/Main.scala
text:
```scala
package recfun

object Main {
  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    println("")
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): I@@nt = {
    if (c == 0 || c == r) 1 
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    def recCheck(chars: List[Char], total: Int): Boolean = {
      if (chars.isEmpty) if (total == 0) true else false
      else if (total < 0) false
      else recCheck(chars.tail, if (chars.head == '(') (total + 1) else if (chars.head == ')') (total - 1) else open)
    }
    recCheck(chars, 0)
  }

  /**
   * Exercise 3 Counting Change
   * Write a recursive function that counts how many different ways you can make
   * change for an amount, given a list of coin denominations. For example,
   * there is 1 way to give change for 5 if you have coins with denomiation
   * 2 and 3: 2+3.
   */
  def countChange(money: Int, coins: List[Int]): Int = {

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

  }

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: scala/Int#