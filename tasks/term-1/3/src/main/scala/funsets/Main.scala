package funsets

object Main extends App {
  import FunSets._
   println("Single sets")
  val sngl1 = singletonSet(1)
  val sngl2 = singletonSet(2) 
  printSet(sngl1)
  printSet(sngl2)
  println("Contains")
  println(contains(sngl1, 1))
  println(contains(sngl2, 3))
  
  println()

  println("Union")
  val sngl3 = singletonSet(3) 
  val un1 = union(sngl1, sngl2)
  val un2 = union(un1, sngl3)
  printSet(un1)
  println()

  println("Intersection")
  printSet(intersect(un1, sngl1))
   println()
  println("Differ")
  
  printSet(diff(un2, sngl1))
   println()
   
   
   println("Filter")
  printSet(filter(un2, x => x % 2 == 0))
  println()

  println("Forall")
  val un3 = filter((x: Int) => x >= 1 && x <= 5, x => x > 0)
  println(forall(un3, x => x > 0))
  println(forall(un3, x => x == 0))
  println()
 

  println("Exists")
  println(exists(un3, x => x > 2))
  println(exists(un3, x => x > 10))
  println()

  println("Map")
  val doubledSet = map(un2, x => x * 2)
  printSet(doubledSet)
   
  
}
