error id: 046B01E7E3DF6E9E826DC8A89D81964C
file://<WORKSPACE>/Main.scala
### java.lang.IndexOutOfBoundsException: 0

occurred in the presentation compiler.



action parameters:
offset: 1009
uri: file://<WORKSPACE>/Main.scala
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

    println("\nBalance test:")
    println(balance("(some test example))((())".toList))
    println(balance("(((((())))))))))))))))".toList))

    println("\nCount Change test:")
    println(countChange(2, List(5, 3)))
    println(countChange(5, List(2, 3)))
    println(countChange(5, List(1, 2)))

    println("\nnQueens test:")
    val solutions = nQueens(10)
    solutions match {
      case Some(solution) => printBoard(solution)
      case None => println("Solution didn't founded!")
    }
  
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || c == r) 1
    else if (c < 0 || c > r) 0
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /**
   * Exercise 2 Parentheses Balancing
   */
  def balance(chars: List[Char]): Boolean = {
    val i@@ counter = 0;
    for (char <- chars) {
      if (counter < 0) return false
      char match {
        case '(' => counter += 1
        case ')' => counter -= 1
        case _ =>
      }
    }
    counter == 0
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
  def isSafe(queens: Array[Int], row: Int, col: Int): Boolean = {
    for (i <- 0 until row) {
      if (queens(i) == col || math.abs(queens(i) - col) == math.abs(i - row)) {
        return false
      }
    }
    true
  }
  
  def solve(queens: Array[Int], row: Int): Boolean = {
    if (row == size) true
    else {
      for (col <- 0 until size) {
        if (isSafe(queens, row, col)) {
          queens(row) = col
          if (solve(queens, row + 1)) return true
        }
      }
      false
    }
  }
  
  val queens = new Array[Int](size)
  if (solve(queens, 0)) Some(queens) else None
}

def printBoard(queens: Array[Int]): Unit = {
  val size = queens.length
  
  for (row <- 0 until size) {
    for (col <- 0 until size) {
      if (queens(row) == col) print("1 ") 
      else print("0 ")
    }
    println()
  }
  println()
  

}

}
```


presentation compiler configuration:
Scala version: 3.7.3-bin-nonbootstrapped
Classpath:
<WORKSPACE>/.scala-build/2_d5c0a6989e/classes/main [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala3-library_3/3.7.3/scala3-library_3-3.7.3.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.16/scala-library-2.13.16.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/sourcegraph/semanticdb-javac/0.10.0/semanticdb-javac-0.10.0.jar [exists ], <WORKSPACE>/.scala-build/2_d5c0a6989e/classes/main/META-INF/best-effort [missing ]
Options:
-Xsemanticdb -sourceroot <WORKSPACE> -Ywith-best-effort-tasty




#### Error stacktrace:

```
scala.collection.LinearSeqOps.apply(LinearSeq.scala:131)
	scala.collection.LinearSeqOps.apply$(LinearSeq.scala:128)
	scala.collection.immutable.List.apply(List.scala:79)
	dotty.tools.pc.InferCompletionType$.inferType(InferExpectedType.scala:94)
	dotty.tools.pc.InferCompletionType$.inferType(InferExpectedType.scala:62)
	dotty.tools.pc.completions.Completions.advancedCompletions(Completions.scala:523)
	dotty.tools.pc.completions.Completions.completions(Completions.scala:122)
	dotty.tools.pc.completions.CompletionProvider.completions(CompletionProvider.scala:139)
	dotty.tools.pc.ScalaPresentationCompiler.complete$$anonfun$1(ScalaPresentationCompiler.scala:197)
```
#### Short summary: 

java.lang.IndexOutOfBoundsException: 0