package funsets

object Main extends App {
  import FunSets._

  val s = singletonSet(1)
  val t = singletonSet(2)
  val u = union(s, t)         

  println("u = " + toString(u))               

  println("forall(u, _ < 3) = " + forall(u, _ < 3))  
  println("forall(u, _ % 2 == 0) = " + forall(u, _ % 2 == 0))

  println("exists(u, _ == 2) = " + exists(u, _ == 2)) 
  println("exists(u, _ == 3) = " + exists(u, _ == 3)) 

  val m = map(u, _ * 2)        
  println("map(u, _ * 2) = " + toString(m))
}
