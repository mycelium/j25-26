package funsets

object Main extends App {
  import FunSets._

  println("\n-----Test for singletonSet and contains-----")
  val single = singletonSet(5)
  println(s"contains(singletonSet(5), 5) = ${contains(single, 5)}") // true
  println(s"contains(singletonSet(5), 3) = ${contains(single, 3)}") // false
  println()

  println("-----Test for union-----")
  val set1 = singletonSet(1)
  val set2 = singletonSet(2)
  val unionSet = union(set1, set2)
  println(s"contains(union({1}, {2}), 1) = ${contains(unionSet, 1)}") // true
  println(s"contains(union({1}, {2}), 3) = ${contains(unionSet, 3)}") // false
  println()

  println("-----Test for intersect-----")
  val setA = union(singletonSet(1), singletonSet(2))
  val setB = union(singletonSet(2), singletonSet(3))
  val intersectSet = intersect(setA, setB)
  println(s"contains(intersect({1,2}, {2,3}), 1) = ${contains(intersectSet, 1)}") // false
  println(s"contains(intersect({1,2}, {2,3}), 2) = ${contains(intersectSet, 2)}") // true
  println()

  println("-----Test for diff-----")
  val diffSet = diff(setA, setB)
  println(s"contains(diff({1,2}, {2,3}), 1) = ${contains(diffSet, 1)}") // true
  println(s"contains(diff({1,2}, {2,3}), 2) = ${contains(diffSet, 2)}") // false
  println()

  println("-----Test for filter-----")
  val numbers = union(union(singletonSet(1), singletonSet(2)), singletonSet(3))
  val evenFilter = filter(numbers, x => x % 2 == 0)
  println(s"contains(filter({1,2,3}, even), 1) = ${contains(evenFilter, 1)}") // false
  println(s"contains(filter({1,2,3}, even), 2) = ${contains(evenFilter, 2)}") // true
  println()

  println("-----Test for forall-----")
  val positiveSet = filter((x: Int) => x >= 1 && x <= 5, x => x > 0)
  val allPositive = forall(positiveSet, x => x > 0)
  println(s"forall({1,2,3,4,5}, positive) = $allPositive") // true

  val mixedSet = union(positiveSet, singletonSet(-1))
  val allPositiveMixed = forall(mixedSet, x => x > 0)
  println(s"forall({1,2,3,4,5,-1}, positive) = $allPositiveMixed") // false
  println()

  println("-----Test for exists-----")
  val hasEven = exists(numbers, x => x % 2 == 0)
  println(s"exists({1,2,3}, even) = $hasEven") // true

  val hasNegative = exists(numbers, x => x < 0)
  println(s"exists({1,2,3}, negative) = $hasNegative") // false
  println()

  println("-----Test for map-----")
  val doubledSet = map(numbers, x => x * 2)
  println(s"contains(map({1,2,3}, double), 2) = ${contains(doubledSet, 2)}") // true
  println(s"contains(map({1,2,3}, double), 3) = ${contains(doubledSet, 3)}") // false
  println()
}