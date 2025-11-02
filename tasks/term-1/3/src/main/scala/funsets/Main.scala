package funsets

object Main extends App {
  import FunSets._

  // Тест singletonSet и contains
  val s1 = singletonSet(1)
  println(s"Contains 1 in s1: ${contains(s1, 1)}")  // true
  println(s"Contains 2 in s1: ${contains(s1, 2)}")  // false

  // Тест union
  val s2 = singletonSet(2)
  val s1UnionS2 = union(s1, s2)
  println(s"Union contains 1: ${contains(s1UnionS2, 1)}")  // true
  println(s"Union contains 2: ${contains(s1UnionS2, 2)}")  // true
  println(s"Union contains 3: ${contains(s1UnionS2, 3)}")  // false

  // Тест intersect
  val sIntersect = intersect(s1UnionS2, s2)
  println(s"Intersect contains 1: ${contains(sIntersect, 1)}")  // false
  println(s"Intersect contains 2: ${contains(sIntersect, 2)}")  // true

  // Тест diff
  val sDiff = diff(s1UnionS2, s1)
  println(s"Diff contains 1: ${contains(sDiff, 1)}")  // false
  println(s"Diff contains 2: ${contains(sDiff, 2)}")  // true

  // Тест filter
  val sFiltered = filter(s1UnionS2, x => x % 2 == 0)
  println(s"Filter even numbers contains 1: ${contains(sFiltered, 1)}")  // false
  println(s"Filter even numbers contains 2: ${contains(sFiltered, 2)}")  // true

  // Тест forall
  println(s"Forall elements > 0: ${forall(s1UnionS2, x => x > 0)}")  // true
  println(s"Forall elements > 1: ${forall(s1UnionS2, x => x > 1)}")  // false

  // Тест exists
  println(s"Exists element == 1: ${exists(s1UnionS2, x => x == 1)}")  // true
  println(s"Exists element == 3: ${exists(s1UnionS2, x => x == 3)}")  // false

  // Тест map
  val sMapped = map(s1UnionS2, x => x * x)
  println(s"Mapped set contains 1 (1 squared): ${contains(sMapped, 1)}")  // true
  println(s"Mapped set contains 4 (2 squared): ${contains(sMapped, 4)}")  // true
  println(s"Mapped set contains 2: ${contains(sMapped, 2)}")             // false

  // Печать множества
  printSet(s1UnionS2)  // {1,2}
  printSet(sMapped)    // {1,4}
}
