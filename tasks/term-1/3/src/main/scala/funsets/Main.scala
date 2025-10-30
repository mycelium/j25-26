// Множество является не структурой данных, а функцией-предикатом, поэтому его элементы не хранятся в физичесткой памяти.
package funsets

object Main extends App {
  import FunSets._

  val set1 = singletonSet(1)
  val set2 = singletonSet(2)
  val set3 = singletonSet(3)

  printSet(set1)
  printSet(set2)
  printSet(set3)

  val unitedSet = union(set1, set2)
  println("union set1 и set2:")
  printSet(unitedSet)
  println()

  val intersected = intersect(unitedSet, set2)
  println("intersect unitedSet и set2:")
  printSet(intersected)
  println()

  val difference = diff(unitedSet, set2)
  println("diff unitedSet и set2:")
  printSet(difference)
  println()

  val filtered = filter(unitedSet, x => x % 2 == 0)
  println("filter even:")
  printSet(filtered)
  println()

  println("forall:")
  println(forall(unitedSet, x => x > 0))
  println(forall(unitedSet, x => x < 2))
  println()

  println("exists:")
  println(exists(unitedSet, x => x == 2))
  println(exists(unitedSet, x => x == 5))
  println()

  val mapped = map(unitedSet, x => x * 2)
  println("map (*2)")
  printSet(mapped)
}