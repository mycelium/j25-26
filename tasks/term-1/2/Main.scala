package recfun
import common._

object Main {
  object Project {

  def pascal(c: Int, r: Int): Int = {
    if(c<=0 || r<=0 || c>r) 0
    else if(c==1 || c==r) 1
    else pascal(c-1, r-1) + pascal(c, r-1)
  }

  def balance(chars: List[Char]): Boolean = {
    if (chars.count(_=='(') == chars.count(_==')')) true
    else false
  }
  
  def countChange(money: Int, coins: List[Int]): Int = {
    if(money < 0) 0
    else if(money == 0) 1
    else ((coins.distinct).map(i => countChange(money - i, coins.slice(coins.indexOf(i), coins.size)))).sum
  }
  
  def nQueens(size: Int): Option[Array[Int]] = {
    def recursiveQueenPlacing(placed: Array[Int], size: Int, row: Int): Option[Array[Int]] = {
      for( i <- 0 to size - 1){
        val allowed = (0 to row-1).forall(j => placed(j) != i && placed(j) != i - row + j && placed(j) != i + row - j)
        if(allowed){
          placed(row) = i
          if(row == size - 1) return Some(placed)
          else{
            val res = recursiveQueenPlacing(placed, size, row+1)
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
  
	def main(args: Array[String]): Unit = {
	  
	  for(i <- 1 to 10){
	    for(j <- 1 to i){
	      print(pascal(j,i) + " ")
	    }
	    println()
	  }
	  
    println(balance("(2+3) * (8 - 8)".toList))
    println(balance("(2+3) * (8 - 8".toList))
    println(balance(")2+3( * (8 - 8)".toList))
    
    println(countChange(5, List(1,1,1)))
	  println(countChange(10, List(5,2,1)))
	  
	  
	  nQueensPrintTable(5, nQueens(5))
	  nQueensPrintTable(8, nQueens(8))
	}
}

}
