package funsets

object Main extends App {
  import FunSets._
  println("Test FunSets.scala:")
  val s1 = singletonSet(1)
  val s2 = singletonSet(2)

  println("--singletonSet: --")
  println("contains(singletonSet(1), 1) = " + contains(singletonSet(1), 1))
  println("contains({1}, 1) = " + contains(s1, 1))
  println("contains({1}, 2) = " + contains(s1, 2))
  println("contains({2}, 2) = " + contains(s2, 2))

  println("--union: --")
  val s12 = union(s1, s2)
  println("union({1}, {2}) contains 1 = " + contains(s12, 1))
  println("union({1}, {2}) contains 2 = " + contains(s12, 2))
  println("union({1}, {2}) contains 3 = " + contains(s12, 3))

  println("--intersect: --")
  val inter = intersect(s12, s2)
  println("intersect({1,2}, {2}) contains 1) = " + contains(inter, 1))
  println("intersect({1,2}, {2}) contains 2) = " + contains(inter, 2))

  println("--diff: --")
  val sDiff = diff(s12, s1)
  println("diff({1,2}, {1}) contains 1) = " + contains(sDiff, 1))
  println("diff({1,2}, {1}) contains 2) = " + contains(sDiff, 2))

  println("--filter: --")
  val sFilt = filter(s12, x => x % 2 == 0)
  println("filter({1,2}, x%2==0) contains 1) = " + contains(sFilt, 1))
  println("filter({1,2}, x%2==0) contains 2) = " + contains(sFilt, 2))

  println("--map: --")
  val sMap = map(s12, x => x * 10)
  println("map({1,2}, x*10) contains 10) = " + contains(sMap, 10))
  println("map({1,2}, x*10) contains 20) = " + contains(sMap, 20))
  println("map({1,2}, x*10) contains 2) = " + contains(sMap, 2))

  println("--printSet: --")
  val sPrint = union(union(singletonSet(15), singletonSet(20)), singletonSet(30))
  println("sPrint: ")
  printSet(sPrint)
}
