package funsets

object Main extends App {
  import FunSets._

  // Test singletonSet
  val s1 = singletonSet(1)
  val s2 = singletonSet(2)
  val s3 = singletonSet(3)

  println("Test singletonSet:")
  println(s"1 in s1? ${contains(s1, 1)}")
  println(s"2 in s1? ${contains(s1, 2)}")

  // Union
  val s = union(s1, union(s2, s3))
  println("\nUnion of s1, s2, s3:")
  printSet(s)

  // Intersection
  val i = intersect(s, singletonSet(2))
  println("\nIntersection with {2}:")
  printSet(i)

  // Diff
  val d = diff(s, singletonSet(3))
  println("\nDifference with {3}:")
  printSet(d)

  // Filter
  val f = filter(s, x => x % 2 == 1)
  println("\nFilter odd numbers:")
  printSet(f)

  // forall
  val testSet: Set = x => x >= 1 && x <= 5
  println(s"forall(testSet, x => x > 0) = ${forall(testSet, x => x > 0)}")
  println(s"forall(testSet, x => x > 3) = ${forall(testSet, x => x > 3)}")
  println(s"forall(testSet, x => x > 3) = ${forall(testSet,  x => x % 2 == 0)}")
  // exists

  println(s"exists(testSet, x => x == 3) = ${exists(testSet, x => x % 2 == 0)}")
  println(s"exists(testSet, x => x == 3) = ${exists(testSet, x => x == 3)}")
  println(s"exists(testSet, x => x > 10) = ${exists(testSet, x => x > 10)}")
  // Map
  val mapped = map(s, x => x * x)
  println("\nSquare all elements:")
  printSet(mapped)
}