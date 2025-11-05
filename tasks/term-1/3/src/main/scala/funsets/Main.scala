package funsets

object Main extends App {
  import FunSets._

  println("contains")
  println(contains(singletonSet(1), 1)) // true
  println(contains(singletonSet(5), 20)) // false

  println("union")
  val set1 = union(singletonSet(14), singletonSet(41))
  val set2 = union(singletonSet(2), singletonSet(22))
  println(FunSets.toString(set1)) // {14, 41}
  println(FunSets.toString(union(set1, set2))) // {14, 41, 2, 22}

  println("intersect")
  val set3 = union(singletonSet(8), singletonSet(25))
  val set4 = union(singletonSet(25), singletonSet(58))
  println(FunSets.toString(intersect(set3, set4))) // {25}
  println(FunSets.toString(intersect(set1, set2))) // {}

  println("diff")
  println(FunSets.toString(diff(set1, set2))) // {14, 41}
  println(FunSets.toString(diff(set1, singletonSet(14)))) // {41}

  println("filter")
  val filteredSet1 = filter((x: Int) => x >= 4 && x <= 50, (x: Int) => x % 5 == 0)
  println(FunSets.toString(filteredSet1)) // {5, 10, 15, 20, 25, 30, 35, 40, 45, 50}

  val filteredSet2 = filter((x: Int) => x >= 7 && x <= 42, (x: Int) => x % 7 == 0)
  println(FunSets.toString(filteredSet2)) // {7, 14, 21, 28, 35, 42}

  println("forall")
  val testSet = (x: Int) => x >= -30 && x % 6 == 0 && x <= 30
  println(forall(testSet, (x: Int) => x % 2 == 0)) // true
  println(forall(testSet, (x: Int) => x > 5)) // false

  println("exists")
  println(exists(testSet, (x: Int) => x == 18)) // true
  println(exists(testSet, (x: Int) => x == 9)) // false

  println("map")
  val mappedSet1 = map((x: Int) => x >= 2 && x <= 6, (x: Int) => x * 4)
  println(FunSets.toString(mappedSet1)) // {8, 12, 16, 20, 24}

  val mappedSet2 = map((x: Int) => x >= 11 && x <= 16, (x: Int) => x - 3)
  println(FunSets.toString(mappedSet2)) // {8, 9, 10, 11, 12, 13}
}