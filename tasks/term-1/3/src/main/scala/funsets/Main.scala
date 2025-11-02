package funsets

object Main extends App {
  import FunSets._
  
  println("\n1) singletonSet / contains")
  val s1 = singletonSet(1)
  printSet(s1)
  println(contains(s1, 1)) 
  println(contains(s1, 2))

  println("\n2) union")
  val s2 = singletonSet(2)
  val s12 = union(s1, s2)
  printSet(s12)

  println("\n3) intersect")
  val s13 = union(singletonSet(1), singletonSet(3))
  val only1 = intersect(s1, s13)
  printSet(only1)

  println("\n4) diff")
  val only3 = diff(s13, s1)
  printSet(only3)

  println("\n5) filter")
  val odds = filter(s13, _ % 2 == 1)
  printSet(odds)
  println(contains(odds, 1))
  println(contains(odds, 2)) 

  println("\n6) forall / exists")
  val pos = union(singletonSet(1), singletonSet(2))
  println(forall(pos, _ > 0)) 
  println(exists(pos, _ == 2)) 

  println("\n7) map")
  val sq = map(pos, x => x * x)
  printSet(sq)

}
