package funsets

object Main extends App {
  import FunSets._

  val set1: Set = x => x > 0 && x < 5
  val set2: Set = x => x > 5 && x < 10

  println("1 in " + FunSets.toString(set1) + ": " + contains(set1, 1))
  println("10 in " + FunSets.toString(set1) + ": " + contains(set1, 10))

  val setUnion = union(set1, singletonSet(10))
  println(FunSets.toString(set1) + " + " + FunSets.toString(singletonSet(10)) + " = " + FunSets.toString(setUnion))

  val setIntersect = intersect(set1, setUnion)
  println(FunSets.toString(set1) + " * " + FunSets.toString(setUnion) + " = " + FunSets.toString(setIntersect))

  val setDiff = diff(setUnion, setIntersect)
  println(FunSets.toString(setUnion) + " / " + FunSets.toString(setIntersect) + " = " + FunSets.toString(setDiff))

  val setFiltered = filter(setUnion, x => x > 2)
  println(FunSets.toString(setUnion) + " with filter 'x => x > 2' = " + FunSets.toString(setFiltered))

  println("forall 'x => x % 2 == 0' in " + FunSets.toString(setUnion) + ": " + forall(setUnion, x => x % 2 == 0))
  println("exists 'x => x % 2 == 0' in" + FunSets.toString(setUnion) + ": " + exists(setUnion, x => x % 2 == 0))
}
