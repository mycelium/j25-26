package funsets

object Main extends App {
  import FunSets._
   println("Single sets")
  val sngl1 = singletonSet(1)
  val sngl2 = singletonSet(2) 
  println("Contains")
  println(contains(sngl1, 1))
  println(contains(sngl2, 3))
  println()

  println("Union")
  val un1 = union(sngl1, sngl2)
  printSet(un1)
  println()

  println("Intersection")
}
